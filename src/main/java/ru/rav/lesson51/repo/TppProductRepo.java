package ru.rav.lesson51.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rav.lesson51.model.TppProduct;

import java.math.BigInteger;

@Repository
public interface TppProductRepo extends CrudRepository<TppProduct, Long> {
    TppProduct findFirstById(BigInteger id);

    TppProduct findFirstByContNumber(String number);
}
