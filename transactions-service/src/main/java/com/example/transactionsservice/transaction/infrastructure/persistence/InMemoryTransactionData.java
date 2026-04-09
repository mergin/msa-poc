package com.example.transactionsservice.transaction.infrastructure.persistence;

import com.example.transactionsservice.transaction.domain.Transaction;
import com.example.transactionsservice.transaction.domain.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

final class InMemoryTransactionData {

  static final List<Transaction> DATA = buildSeedData();

  private InMemoryTransactionData() {}

  private static List<Transaction> buildSeedData() {
    String[] categories = {"GROCERIES", "UTILITIES", "SALARY", "RENT", "ENTERTAINMENT", "TRANSFER"};
    String[] accounts = {"a-001", "a-002", "a-003", "a-004", "a-005"};
    List<Transaction> list = new java.util.ArrayList<>();
    int idx = 0;
    for (int a = 0; a < accounts.length; a++) {
      for (int t = 1; t <= 20; t++) {
        idx++;
        TransactionType type = t % 3 == 0 ? TransactionType.CREDIT : TransactionType.DEBIT;
        BigDecimal amount = new BigDecimal(50 + idx * 13).setScale(2, RoundingMode.HALF_UP);
        list.add(
            new Transaction(
                "tx-" + String.format("%03d", idx),
                accounts[a],
                type,
                amount,
                "EUR",
                type.name().toLowerCase() + " #" + idx,
                LocalDateTime.now().minusDays(idx),
                categories[idx % categories.length]));
      }
    }
    return List.copyOf(list);
  }
}