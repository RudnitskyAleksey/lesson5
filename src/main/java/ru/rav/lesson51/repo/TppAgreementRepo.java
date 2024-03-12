package ru.rav.lesson51.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rav.lesson51.model.Agreement;

@Repository
public interface TppAgreementRepo extends CrudRepository<Agreement, Long> {
}
