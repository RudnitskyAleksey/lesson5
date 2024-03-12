package ru.rav.lesson51.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rav.lesson51.model.Agreement;

import java.math.BigInteger;

@Repository
public interface AgreementRepo extends CrudRepository<Agreement, Long> {
    Agreement findFirstById(BigInteger id);
    Agreement findFirstByNumber(String number);
}
