package com.banking;

import com.banking.models.dto.request.GetAccountTransactionsRequestDTO;
import com.banking.models.dto.response.TransactionResponseDTO;
import com.banking.models.entities.Account;
import com.banking.models.entities.Transaction;
import com.banking.models.entities.User;
import com.banking.models.enums.AccountType;
import com.banking.models.enums.TransactionType;
import com.banking.models.enums.UserRole;
import com.banking.models.enums.UserStatus;
import com.banking.repositories.AccountRepository;
import com.banking.repositories.TransactionRepository;
import com.banking.repositories.UserRepository;
import com.banking.services.AccountService;
import com.banking.services.Interface.ITransactionService;
import com.banking.services.TransactionService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class BankingApplicationTests {
	@Autowired
	private ITransactionService transactionService;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AccountService accountService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Test
	@Transactional
	void test_Transaction_GetById() {
		// Create data
		User user1 = createDefaultUser(1);

		Account account1 = createDefaultAccount(user1, "1000.00");
		Account account2 = createDefaultAccount(user1, "8000.00");

		Transaction trans1 = createDefaultTransaction(null, account1, "100.00"); // Create withdraw transaction
		Transaction trans2 = createDefaultTransaction(account2, null, "200.00"); // Create deposit transaction
		Transaction trans3 = createDefaultTransaction(account2, account1, "300.00"); // Create transaction between accounts

		// Check withdraw transaction
		var responseTrans1 = transactionService.getTransactionById(trans1.getTransactionId());
		Assertions.assertEquals(trans1.getTransactionId(), responseTrans1.getTransactionId());

		// Check deposit transaction
		var responseTrans2 = transactionService.getTransactionById(trans2.getTransactionId());
		Assertions.assertEquals(trans2.getTransactionId(), responseTrans2.getTransactionId());

		// Check transaction between accounts
		var responseTrans3 = transactionService.getTransactionById(trans3.getTransactionId());
		Assertions.assertEquals(trans3.getTransactionId(), responseTrans3.getTransactionId());
	}

	@Test
	@Transactional
	void test_Transaction_AccountTransactionCount() {
		// Create data
		User user1 = createDefaultUser(1);

		Account account1 = createDefaultAccount(user1, "1000.00");
		Account account2 = createDefaultAccount(user1, "8000.00");

		// Create transactions
		createDefaultTransaction(null, account1, "100.00");
		createDefaultTransaction(account1, account2, "100.00");
		createDefaultTransaction(account1, null, "100.00");
		createDefaultTransaction(null, account1, "100.00");
		createDefaultTransaction(account1, account2, "100.00");
		createDefaultTransaction(account2, account1, "100.00");
		createDefaultTransaction(null, account2, "100.00");
		createDefaultTransaction(account2, null, "100.00");
		createDefaultTransaction(account1, account2, "100.00");

		// Check account1 count
		var count1 = transactionService.getAccountTransactionsCount(account1.getAccountId());
		Assertions.assertEquals(7, count1.getCount());

		// Check account2 count
		var count2 = transactionService.getAccountTransactionsCount(account2.getAccountId());
		Assertions.assertEquals(6, count2.getCount());
	}

	@Test
	@Transactional
	void test_Transaction_AccountTransactions() {
		// Create data
		User user1 = createDefaultUser(1);

		Account account1 = createDefaultAccount(user1, "1000.00");
		Account account2 = createDefaultAccount(user1, "8000.00");

		// Create transactions
		Transaction trans1 = createDefaultTransaction(null, account1, "100.00");
		Transaction trans2 = createDefaultTransaction(account1, account2, "100.00");
		Transaction trans3 = createDefaultTransaction(account1, null, "100.00");
		Transaction trans4 = createDefaultTransaction(null, account1, "100.00");
		Transaction trans5 = createDefaultTransaction(account1, account2, "100.00");
		Transaction trans6 = createDefaultTransaction(account2, account1, "100.00");
		Transaction trans7 = createDefaultTransaction(null, account2, "100.00");
		Transaction trans8 = createDefaultTransaction(account2, null, "100.00");
		Transaction trans9 = createDefaultTransaction(account1, account2, "100.00");

		// Create list of account1 transactions id's
		List<UUID> account1Transactions = new ArrayList<UUID>();
		account1Transactions.add(trans1.getTransactionId());
		account1Transactions.add(trans2.getTransactionId());
		account1Transactions.add(trans3.getTransactionId());
		account1Transactions.add(trans4.getTransactionId());
		account1Transactions.add(trans5.getTransactionId());
		account1Transactions.add(trans6.getTransactionId());
		account1Transactions.add(trans9.getTransactionId());

		// Create list of account2 transactions id's
		List<UUID> account2Transactions = new ArrayList<UUID>();
		account2Transactions.add(trans2.getTransactionId());
		account2Transactions.add(trans5.getTransactionId());
		account2Transactions.add(trans6.getTransactionId());
		account2Transactions.add(trans7.getTransactionId());
		account2Transactions.add(trans8.getTransactionId());
		account2Transactions.add(trans9.getTransactionId());

		// Check account1 transactions
		var res1 = transactionService.getAccountTransactions(GetAccountTransactionsRequestDTO.builder()
				.accountId(account1.getAccountId())
				.pageNumber(1)
				.transactionsPerPage(24)
				.build());

		for (int i = 0; i < res1.size(); i++) {
			if(!account1Transactions.contains(res1.get(i).getTransactionId())){
				Assertions.fail();
			}
		}

		// Check account2 transactions
		var res2 = transactionService.getAccountTransactions(GetAccountTransactionsRequestDTO.builder()
				.accountId(account2.getAccountId())
				.pageNumber(1)
				.transactionsPerPage(24)
				.build());

		for (int i = 0; i < res2.size(); i++) {
			if(!account2Transactions.contains(res2.get(i).getTransactionId())){
				Assertions.fail();
			}
		}
	}

	private User createDefaultUser(int number){
		User user = User.builder()
				.firstName("User")
				.lastName("Number " + number)
				.email("user.n" + number + "@email.com")
				.bsn("2312312" + number)
				.role(UserRole.CUSTOMER)
				.phoneNumber("+311112222")
				.status(UserStatus.ACTIVE)
				.passwordHash(passwordEncoder.encode("password123"))
				.build();
		userRepository.save(user);

		return user;
	}

	private Account createDefaultAccount(User user, String balance){
		return createCustomAccount(user, AccountType.CHECKING, "1000.00", "0.00", "300.00", "0.00", balance);
	}

	private Account createCustomAccount(User user, AccountType type, String dailyLimit, String todayChange, String transferLimit, String absoluteMinimum, String balance){
		Account account = Account.builder()
				.user(user)
				.iban(accountService.generateIban())
				.accountType(type)
				.dailyLimit(new BigDecimal(dailyLimit))
				.todayChange(new BigDecimal(todayChange))
				.transferLimit(new BigDecimal(transferLimit))
				.absoluteMinimum(new BigDecimal(absoluteMinimum))
				.balance(new BigDecimal(balance))
				.build();
		accountRepository.save(account);

		return account;
	}

	private Transaction createDefaultTransaction(Account fromAccount, Account toAccount, String amount){
		return createCustomTransaction(fromAccount, toAccount, amount, "Default transaction", TransactionType.CUSOMER_TRANSFER);
	}

	private Transaction createCustomTransaction(Account fromAccount, Account toAccount, String amount, String desc, TransactionType type){
		Transaction trans = Transaction.builder()
				.fromAccount(fromAccount)
				.toAccount(toAccount)
				.amount(new BigDecimal(amount))
				.description(desc)
				.type(type)
				.createdAt(LocalDateTime.now())
				.build();
		transactionRepository.save(trans);

		return trans;
	}
}
