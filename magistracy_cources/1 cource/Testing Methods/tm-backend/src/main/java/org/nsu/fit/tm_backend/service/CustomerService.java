package org.nsu.fit.tm_backend.service;

import java.util.Set;
import java.util.UUID;
import org.jvnet.hk2.annotations.Contract;
import org.nsu.fit.tm_backend.repository.data.ContactPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;

@Contract
public interface CustomerService {
    /**
     * Метод создает новый объект класса Customer. Ограничения:
     * Аргумент 'customer' - не null;
     * firstName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * lastName - нет пробелов, длина от 2 до 12 символов включительно, начинается с заглавной буквы, остальные символы строчные, нет цифр и других символов;
     * login - указывается в виде email, проверить email на корректность, проверить что нет customer с таким же email;
     * pass - длина от 6 до 12 символов включительно, не должен быть простым (123qwe или 1q2w3e), не должен содержать части login, firstName, lastName
     * balance - должно быть равно 0 перед отправкой базу данных.
     */
    CustomerPojo createCustomer(CustomerPojo customer);

    Set<CustomerPojo> getCustomers();

    Set<UUID> getCustomerIds();

    CustomerPojo getCustomer(UUID customerId);

    CustomerPojo lookupCustomer(UUID customerId);

    CustomerPojo lookupCustomer(String login);

    ContactPojo me(AuthenticatedUserDetails authenticatedUserDetails);

    void deleteCustomer(UUID id);

    /**
     * Метод добавляет к текущему балансу переданное значение, которое должно быть строго больше нуля.
     */
    CustomerPojo topUpBalance(UUID customerId, Integer money);
}
