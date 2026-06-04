package com.hfinance.domain.model;

import com.hfinance.domain.enums.PaymentMethod;
import com.hfinance.domain.enums.RecurrenceType;
import com.hfinance.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private Long accountId;
    private Long categoryId;
    private LocalDate transactionDate;
    private TransactionType transactionType;
    private PaymentMethod paymentMethod;
    private String description;
    private BigDecimal amount;
    private String recurrenceGroupId;
    private RecurrenceType recurrenceType;
    private Integer recurrenceIndex;
    private Integer recurrenceTotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Transaction(Long id, Long accountId, Long categoryId, LocalDate transactionDate,
                       TransactionType transactionType, PaymentMethod paymentMethod, String description,
                       BigDecimal amount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, accountId, categoryId, transactionDate, transactionType, paymentMethod, description, amount,
                null, RecurrenceType.NONE, null, null, createdAt, updatedAt);
    }

    public Transaction(Long id, Long accountId, Long categoryId, LocalDate transactionDate,
                       TransactionType transactionType, PaymentMethod paymentMethod, String description,
                       BigDecimal amount, String recurrenceGroupId, RecurrenceType recurrenceType,
                       Integer recurrenceIndex, Integer recurrenceTotal,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.amount = amount;
        this.recurrenceGroupId = recurrenceGroupId;
        this.recurrenceType = recurrenceType == null ? RecurrenceType.NONE : recurrenceType;
        this.recurrenceIndex = recurrenceIndex;
        this.recurrenceTotal = recurrenceTotal;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Transaction newTransaction(Long accountId, Long categoryId, LocalDate transactionDate,
                                             TransactionType transactionType, PaymentMethod paymentMethod,
                                             String description, BigDecimal amount) {
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(null, accountId, categoryId, transactionDate, transactionType, paymentMethod,
                description == null ? "" : description, amount, now, now);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRecurrenceGroupId() {
        return recurrenceGroupId;
    }

    public void setRecurrenceGroupId(String recurrenceGroupId) {
        this.recurrenceGroupId = recurrenceGroupId;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType == null ? RecurrenceType.NONE : recurrenceType;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType == null ? RecurrenceType.NONE : recurrenceType;
    }

    public Integer getRecurrenceIndex() {
        return recurrenceIndex;
    }

    public void setRecurrenceIndex(Integer recurrenceIndex) {
        this.recurrenceIndex = recurrenceIndex;
    }

    public Integer getRecurrenceTotal() {
        return recurrenceTotal;
    }

    public void setRecurrenceTotal(Integer recurrenceTotal) {
        this.recurrenceTotal = recurrenceTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
