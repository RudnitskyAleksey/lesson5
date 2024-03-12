package ru.rav.lesson51.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rav.lesson51.model.Account;

import java.math.BigInteger;
import java.util.Set;

@Repository
public interface AccountRepo extends CrudRepository<Account, Long> {
    Account findFirstById(BigInteger id);

    String select1 =
            "select t3.id id_acc"+
                    "  from tpp_ref_product_register_type t1 " +
                    "  left join account_pool t2 on t1.value = t2.registry_type_code" +
                    "            and t2.branch_code = ?1 and t2.currency_code = ?2" +
                    "            and t2.mdm_code    = ?3 and t2.priority_code = ?4" +
                    "  left join account t3 on t3.account_pool_id = t2.id and t3.bussy = false" +
                    " where t1.value = ?5" +
                    "   and t1.account_type = 'Клиентский'" +
                    "   and (t1.register_type_start_date is null or t1.register_type_start_date <= current_date)" +
                    "   and (t1.register_type_end_date   is null or t1.register_type_end_date   >= current_date)" +
                    " order by t2.priority_code nulls last, t3.account_number nulls last";
    String select2 = "SELECT internal_id FROM tpp_ref_product_class t1 WHERE t1.value = ?1";

    @Query(value = select1, nativeQuery = true)
    Set<BigInteger> MyAccountId( String branchCode, String currCode, String mdmCode, String priorityCode, String regType);

    @Query(value = select2, nativeQuery = true)
    BigInteger MyProductId( String productCode);
}
