package org.nsu.fit.tm_backend.impl;

import java.rmi.server.UID;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.ContactPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.impl.CustomerServiceImpl;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.Globals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Лабораторная 2: покрыть unit тестами класс CustomerServiceImpl на 100%.
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    @Mock
    private Repository repository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerPojo getRightJohn() {
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Baba_Jaga";
        createCustomerInput.balance = 0;
        return createCustomerInput;
    }

    private CustomerPojo getRightJohnRepository() {
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.id = UUID.randomUUID();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Baba_Jaga";
        createCustomerInput.balance = 0;
        return createCustomerInput;
    }

    @Test
    void testCreateCustomer() {
        // arrange: готовим входные аргументы и настраиваем mock'и.
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "Baba_Jaga";
        createCustomerInput.balance = 0;

        CustomerPojo createCustomerOutput = new CustomerPojo();
        createCustomerOutput.id = UUID.randomUUID();
        createCustomerOutput.firstName = "John";
        createCustomerOutput.lastName = "Wick";
        createCustomerOutput.login = "john_wick@example.com";
        createCustomerOutput.pass = "Baba_Jaga";
        createCustomerOutput.balance = 0;

        when(repository.createCustomer(createCustomerInput)).thenReturn(createCustomerOutput);

        // act: вызываем метод, который хотим протестировать.
        CustomerPojo customer = customerService.createCustomer(createCustomerInput);

        // assert: проверяем результат выполнения метода.
        assertEquals(customer.id, createCustomerOutput.id);

        // Проверяем, что метод по созданию Customer был вызван ровно 1 раз с определенными аргументами
        verify(repository, times(1)).createCustomer(createCustomerInput);

        // Проверяем, что другие методы не вызывались...
        verify(repository, times(0)).getCustomers();
    }

    // Как не надо писать тест...
    @Test
    void testCreateCustomerWithNullArgument_Wrong() {
        try {
            customerService.createCustomer(null);
        } catch (IllegalArgumentException ex) {
            assertEquals("Argument 'customer' is null.", ex.getMessage());
        }
    }

    @Test
    void testCreateCustomerWithNullArgument_Right() {
        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                customerService.createCustomer(null));
        assertEquals("Argument 'customer' is null.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithShortPassword() {
        // arrange
        CustomerPojo createCustomerInput = new CustomerPojo();
        createCustomerInput.firstName = "John";
        createCustomerInput.lastName = "Wick";
        createCustomerInput.login = "john_wick@example.com";
        createCustomerInput.pass = "123qwe";
        createCustomerInput.balance = 0;

        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("Password is very easy.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithPasswordNull() {
        // arrange
        CustomerPojo createCustomerInput = getRightJohn();
        createCustomerInput.pass = null;
        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("Field 'customer.pass' is null.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithPlaceInFirstName() {
        // arrange
        CustomerPojo createCustomerInput = getRightJohn();
        createCustomerInput.firstName = "Joh n";
        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("FirstName contains space.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithLongPassword() {
        // arrange
        CustomerPojo createCustomerInput = getRightJohn();
        createCustomerInput.pass = "Baba_JagaBaba_JagaBaba_JagaBaba_Jaga";
        // act-assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(createCustomerInput));
        assertEquals("Password's length should be more or equal 6 symbols and less or equal 12 symbols.", exception.getMessage());
    }

    @Test
    void testCreateCustomerWithOtherUserLogin() {
        // arrange
        CustomerPojo customerRepository1 = getRightJohnRepository();
        CustomerPojo customer2 = getRightJohn();
        when(repository.getCustomerByLogin(customerRepository1.login)).thenReturn(customerRepository1);
        // act: вызываем метод, который хотим протестировать.
        Exception exception = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(customer2));
        assertEquals("Login is busy.", exception.getMessage());
        verify(repository, times(1)).getCustomerByLogin(customerRepository1.login);
        verify(repository, times(0)).getCustomers();
    }

    @Test
    void testGetCustomers() {
        // arrange
        Set<CustomerPojo> customers = new HashSet<>();
        customers.add(getRightJohnRepository());
        when(repository.getCustomers()).thenReturn(customers);
        // act: вызываем метод, который хотим протестировать.
        Set<CustomerPojo> res = customerService.getCustomers();
        assertEquals(res.size(), customers.size());
        verify(repository, times(1)).getCustomers();
        verify(repository, times(0)).getCustomerIds();
    }

    @Test
    void testGetCustomerIds() {
        // arrange
        UUID id = UUID.randomUUID();
        Set<UUID> ids = new HashSet<>();
        ids.add(id);
        when(repository.getCustomerIds()).thenReturn(ids);
        // act: вызываем метод, который хотим протестировать.
        Set<UUID> res = customerService.getCustomerIds();
        assertEquals(res, ids);
        verify(repository, times(0)).getCustomers();
        verify(repository, times(1)).getCustomerIds();
    }

    @Test
    void testGetCustomer() {
        CustomerPojo customer = getRightJohnRepository();
        when(repository.getCustomer(customer.id)).thenReturn(customer);
        // act: вызываем метод, который хотим протестировать.
        CustomerPojo res = customerService.getCustomer(customer.id);
        assertEquals(res.id, customer.id);
        assertEquals(res.login, customer.login);
        verify(repository, times(1)).getCustomer(customer.id);
        verify(repository, times(0)).getCustomerIds();
    }

    @Test
    void testLookupCustomerByIdFind() {
        CustomerPojo customer = getRightJohnRepository();
        CustomerPojo customer1 = getRightJohnRepository();
        Set<CustomerPojo> customers = new HashSet<>();
        customers.add(customer);
        customers.add(customer1);
        when(repository.getCustomers()).thenReturn(customers);
        // act: вызываем метод, который хотим протестировать.
        CustomerPojo res = customerService.lookupCustomer(customer.id);
        assertEquals(res.id, customer.id);
        assertEquals(res.login, customer.login);
        verify(repository, times(0)).getCustomer(customer.id);
        verify(repository, times(1)).getCustomers();
    }

    @Test
    void testLookupCustomerByIdNotFind() {
        CustomerPojo customer = getRightJohnRepository();
        CustomerPojo customer1 = getRightJohnRepository();
        Set<CustomerPojo> customers = new HashSet<>();
        customers.add(customer);
        when(repository.getCustomers()).thenReturn(customers);
        // act: вызываем метод, который хотим протестировать.
        CustomerPojo res = customerService.lookupCustomer(customer1.id);
        assertEquals(res, null);
        verify(repository, times(0)).getCustomer(customer.id);
        verify(repository, times(1)).getCustomers();
    }

    @Test
    void testLookupCustomerByLoginFind() {
        CustomerPojo customer = getRightJohnRepository();
        CustomerPojo customer1 = getRightJohnRepository();
        customer1.login = "Mike";
        Set<CustomerPojo> customers = new HashSet<>();
        customers.add(customer);
        customers.add(customer1);
        when(repository.getCustomers()).thenReturn(customers);
        // act: вызываем метод, который хотим протестировать.
        CustomerPojo res = customerService.lookupCustomer(customer.login);
        assertEquals(res.id, customer.id);
        assertEquals(res.login, customer.login);
        verify(repository, times(0)).getCustomerByLogin(customer.login);
        verify(repository, times(1)).getCustomers();
    }

    @Test
    void testLookupCustomerByLoginNotFind() {
        CustomerPojo customer = getRightJohnRepository();
        CustomerPojo customer1 = getRightJohnRepository();
        customer1.login = "Mike";
        Set<CustomerPojo> customers = new HashSet<>();
        customers.add(customer);
        when(repository.getCustomers()).thenReturn(customers);
        // act: вызываем метод, который хотим протестировать.
        CustomerPojo res = customerService.lookupCustomer(customer1.login);
        assertEquals(res, null);
        verify(repository, times(0)).getCustomerByLogin(customer.login);
        verify(repository, times(1)).getCustomers();
    }

    @Test
    void testMeAdmin() {
        CustomerPojo customer = getRightJohnRepository();
        Set <String> authSet = new HashSet<>();
        authSet.add(Authority.ADMIN_ROLE);
        AuthenticatedUserDetails details = new AuthenticatedUserDetails(
                customer.id.toString(), customer.login, authSet
        );
        ContactPojo res = customerService.me(details);
        assertEquals(res.login, Globals.ADMIN_LOGIN);
        assertEquals(res.getClass(), ContactPojo.class);
    }

    @Test
    void testMeCustomer() {
        CustomerPojo customer = getRightJohnRepository();
        Set <String> authSet = new HashSet<>();
        authSet.add(Authority.CUSTOMER_ROLE);
        AuthenticatedUserDetails details = new AuthenticatedUserDetails(
                customer.id.toString(), customer.login, authSet
        );
        when(repository.getCustomerByLogin(customer.login)).thenReturn(customer);
        ContactPojo res = customerService.me(details);
        assertEquals(res.login, customer.login);
       // assertEquals(res.getClass(), ContactPojo.class);
    }

    @Test
    void testDeleteCustomer() {
        CustomerPojo customer = getRightJohnRepository();
        customerService.deleteCustomer(customer.id);
        verify(repository, times(1)).deleteCustomer(customer.id);
    }

    @Test
    void testTopUpBalance() {
        CustomerPojo customerInput = getRightJohnRepository();
        int value = 10;
        int balance = customerInput.balance;
        when(repository.getCustomer(customerInput.id)).thenReturn(customerInput);
        CustomerPojo res = customerService.topUpBalance(customerInput.id, value);
        assertEquals(res.id, customerInput.id);
        assertEquals(res.login, customerInput.login);
        assertEquals(res.balance, balance + value);
    }
}
