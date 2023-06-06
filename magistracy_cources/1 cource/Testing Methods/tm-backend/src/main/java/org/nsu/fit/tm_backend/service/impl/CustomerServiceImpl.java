package org.nsu.fit.tm_backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.jvnet.hk2.annotations.Service;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.ContactPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Globals;

import javax.inject.Inject;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    @Inject
    private Repository repository;

    /**
     * Метод создает новый объект класса Customer. Ограничения:
     * Аргумент 'customer' - не null;
     * firstName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * lastName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * login - указывается в виде email, проверить email на корректность, проверить что нет customer с таким же email;
     * pass - длина от 6 до 12 символов включительно, не должен быть простым (123qwe или 1q2w3e), не должен содержать части login, firstName, lastName
     * balance - должно быть равно 0 перед отправкой базу данных.
     */
    public CustomerPojo createCustomer(CustomerPojo customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Argument 'customer' is null.");
        }

        if (customer.pass == null) {
            throw new IllegalArgumentException("Field 'customer.pass' is null.");
        }

        if (customer.pass.length() < 6 || customer.pass.length() > 12) {
            throw new IllegalArgumentException("Password's length should be more or equal 6 symbols and less or equal 12 symbols.");
        }

        if (customer.pass.equalsIgnoreCase("123qwe")) {
            throw new IllegalArgumentException("Password is very easy.");
        }

        if (customer.firstName.length() < 2) {
            throw new IllegalArgumentException("First name is very short.");
        }

        // Лабораторная 2: добавить код который бы проверял, что нет customer'а c таким же login (email'ом).
        // Попробовать добавить другие ограничения, посмотреть как быстро растет кодовая база тестов.

        if (repository.getCustomerByLogin(customer.login) != null) {
            throw new IllegalArgumentException("Login is busy.");
        }

        if (customer.firstName.contains(" ")) {
            throw new IllegalArgumentException("FirstName contains space.");
        }

        return repository.createCustomer(customer);
    }

    /**
     * Метод возвращает список customer'ов.
     */
    public Set<CustomerPojo> getCustomers() {
        return repository.getCustomers();
    }

    public Set<UUID> getCustomerIds() {
        return repository.getCustomerIds();
    }

    public CustomerPojo getCustomer(UUID customerId) {
        return repository.getCustomer(customerId);
    }

    public CustomerPojo lookupCustomer(UUID customerId) {
        return repository.getCustomers().stream()
            .filter(x -> x.id.equals(customerId))
            .findFirst()
            .orElse(null);}

    public CustomerPojo lookupCustomer(String login) {
        return repository.getCustomers().stream()
                .filter(x -> x.login.equals(login))
                .findFirst()
                .orElse(null);
    }

    public ContactPojo me(AuthenticatedUserDetails authenticatedUserDetails) {
        ContactPojo contactPojo = new ContactPojo();

        if (authenticatedUserDetails.isAdmin()) {
            contactPojo.login = Globals.ADMIN_LOGIN;

            return contactPojo;
        }

        // Лабораторная 2: обратите внимание, что вернули данных больше чем надо...
        // т.е. getCustomerByLogin честно возвратит все что есть в базе данных по этому customer'у.
        // необходимо написать такой unit тест, который бы отлавливал данное поведение.
        return repository.getCustomerByLogin(authenticatedUserDetails.getName());
    }

    public void deleteCustomer(UUID id) {
        repository.deleteCustomer(id);
    }

    /**
     * Метод добавляет к текущему балансу переданное значение, которое должно быть строго больше нуля.
     */
    public CustomerPojo topUpBalance(UUID customerId, Integer money) {
        var customer = getCustomer(customerId);

        customer.balance += money;

        repository.editCustomer(customer);

        return customer;
    }
}
