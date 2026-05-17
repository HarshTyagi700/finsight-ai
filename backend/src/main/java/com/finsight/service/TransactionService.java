package com.finsight.service;

import com.finsight.dto.TransactionDTO;
import com.finsight.dto.TransactionSummaryDTO;
import com.finsight.entity.Transaction;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.exception.ValidationException;
import com.finsight.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transaction Service
 * Handles all transaction business logic and persistence
 */
@Service
@Slf4j
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private GeminiAIService geminiAIService;
    
    private static final String TRANSACTIONS_TOPIC = "transactions-topic";
    
    /**
     * Create a new transaction
     * If category is missing, auto-categorize using Gemini API
     */
    public Transaction createTransaction(TransactionDTO dto) {
        log.info("Creating transaction for merchant: {}", dto.getMerchant());
        
        // Validate input
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than 0");
        }
        
        // Auto-categorize if category is missing
        String category = dto.getCategory();
        if (category == null || category.isBlank()) {
            log.debug("Auto-categorizing transaction for merchant: {}", dto.getMerchant());
            category = geminiAIService.categorizeTransaction(dto.getMerchant());
        }
        
        // Create and save transaction
        Transaction transaction = new Transaction(
            dto.getMerchant(),
            dto.getAmount(),
            category,
            dto.getPaymentMethod()
        );
        transaction.setDescription(dto.getDescription());
        transaction.setStatus("PENDING");
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {}", savedTransaction.getId());
        
        // Publish to Kafka for async processing
        publishTransactionToKafka(savedTransaction);
        
        return savedTransaction;
    }
    
    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        log.info("Fetching all transactions");
        return transactionRepository.findAllByOrderByTimestampDesc();
    }
    
    /**
     * Get transaction by ID
     */
    public Transaction getTransactionById(String id) {
        log.info("Fetching transaction with ID: {}", id);
        return transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
    }
    
    /**
     * Get transactions by category
     */
    public List<Transaction> getTransactionsByCategory(String category) {
        log.info("Fetching transactions for category: {}", category);
        return transactionRepository.findByCategory(category);
    }
    
    /**
     * Get transactions between date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching transactions between {} and {}", startDate, endDate);
        return transactionRepository.findByTimestampBetween(startDate, endDate);
    }
    
    /**
     * Get transaction summary with aggregated data
     */
    public TransactionSummaryDTO getTransactionSummary() {
        log.info("Generating transaction summary");
        
        List<Transaction> allTransactions = getAllTransactions();
        long totalTransactions = allTransactions.size();
        
        BigDecimal totalSpending = allTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Category breakdown
        Map<String, BigDecimal> categoryBreakdown = allTransactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ));
        
        // Top merchants
        Map<String, BigDecimal> topMerchants = allTransactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getMerchant,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ))
            .entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .collect(Collectors.toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue));
        
        BigDecimal averageTransactionAmount = totalTransactions > 0
            ? totalSpending.divide(new BigDecimal(totalTransactions), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        TransactionSummaryDTO summary = new TransactionSummaryDTO();
        summary.setTotalTransactions(totalTransactions);
        summary.setTotalSpending(totalSpending);
        summary.setCategoryBreakdown(categoryBreakdown);
        summary.setAverageTransactionAmount(averageTransactionAmount);
        summary.setTopMerchants(topMerchants);
        summary.setGeneratedAt(LocalDateTime.now().toString());
        
        log.info("Summary generated: {} transactions, ₹{} total spending", totalTransactions, totalSpending);
        
        return summary;
    }
    
    /**
     * Update transaction status
     */
    public Transaction updateTransactionStatus(String id, String status) {
        log.info("Updating transaction {} status to {}", id, status);
        
        Transaction transaction = getTransactionById(id);
        transaction.setStatus(status);
        transaction.setUpdatedAt(LocalDateTime.now());
        
        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction {} status updated to {}", id, status);
        
        return updated;
    }
    
    /**
     * Delete transaction
     */
    public void deleteTransaction(String id) {
        log.info("Deleting transaction with ID: {}", id);
        
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found with ID: " + id);
        }
        
        transactionRepository.deleteById(id);
        log.info("Transaction {} deleted", id);
    }
    
    /**
     * Publish transaction to Kafka topic
     */
    private void publishTransactionToKafka(Transaction transaction) {
        try {
            String message = String.format(
                "{\"id\":\"%s\",\"merchant\":\"%s\",\"amount\":%s,\"category\":\"%s\",\"status\":\"%s\"}",
                transaction.getId(),
                transaction.getMerchant(),
                transaction.getAmount(),
                transaction.getCategory(),
                transaction.getStatus()
            );
            
            kafkaTemplate.send(TRANSACTIONS_TOPIC, transaction.getId(), message);
            log.info("Transaction {} published to Kafka", transaction.getId());
        } catch (Exception e) {
            log.error("Error publishing transaction to Kafka", e);
            // Don't fail the operation if Kafka fails
        }
    }
    
    /**
     * Bulk create transactions
     */
    public List<Transaction> bulkCreateTransactions(List<TransactionDTO> dtoList) {
        log.info("Bulk creating {} transactions", dtoList.size());
        
        List<Transaction> transactions = new ArrayList<>();
        for (TransactionDTO dto : dtoList) {
            try {
                Transaction transaction = createTransaction(dto);
                transactions.add(transaction);
            } catch (Exception e) {
                log.error("Error creating transaction for merchant: {}", dto.getMerchant(), e);
            }
        }
        
        log.info("Successfully created {} out of {} transactions", transactions.size(), dtoList.size());
        return transactions;
    }
}
