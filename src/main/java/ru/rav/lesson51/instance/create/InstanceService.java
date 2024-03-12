package ru.rav.lesson51.instance.create;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.rav.lesson51.account.create.RegisterService;
import ru.rav.lesson51.model.Agreement;
import ru.rav.lesson51.model.TppProduct;
import ru.rav.lesson51.model.TppProductRegister;
import ru.rav.lesson51.repo.AccountRepo;
import ru.rav.lesson51.repo.AgreementRepo;
import ru.rav.lesson51.repo.TppProductRepo;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Set;

@Service
public class InstanceService {
    private TppProductRepo tppProductRepo;

    private AgreementRepo agreementRepo;

    private AccountRepo accountRepo;

    private ClientMdm clientMdm;

    @Autowired
    public void setTppProductRepo(TppProductRepo tppProductRepo) {
        this.tppProductRepo = tppProductRepo;
    }

    @Autowired
    public void setAgreementRepo(AgreementRepo agreementRepo) {
        this.agreementRepo = agreementRepo;
    }

    @Autowired
    public void setAccountRepo(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Autowired
    public void setClientMdm(ClientMdm clientMdm) {
        this.clientMdm = clientMdm;
    }

    @Getter @Setter
    String message;

    public BigInteger getClientIdForMdm(String mdmCode) {
        System.out.println("before getClientIdForMdm "+mdmCode);
        BigInteger result = clientMdm.getClientIdForMdm( mdmCode);
        System.out.println("after getClientIdForMdm " + result.longValue());
        return result;
    }

    public void setInstance( TppProduct tppProduct) throws SQLException  {
        try {
            RegisterService productService = new RegisterService();
            productService.setProduct( tppProduct, accountRepo);
            tppProductRepo.save(tppProduct);

        }
        catch (Exception ex) {
            throw new SQLException( ex);
        }
    }

    public int setProduct(InstanceCreate instanceCreate, InstanceResult instanceResult) {
        TppProduct tppProduct = new TppProduct();
        BigInteger productCodeId;
        boolean bFind;

        try {
            if (instanceCreate.getInstanceId() != null) {
                tppProduct = tppProductRepo.findFirstById( instanceCreate.getInstanceId() );
                if (tppProduct == null) {
                    this.message = "Отсутствует договор с id = " + instanceCreate.getInstanceId();
                    return 400;
                }
            }
            else {
                BigInteger clientId = getClientIdForMdm(instanceCreate.getMdmCode());
                System.out.println("clientId=" + clientId.longValue());
                if ( clientId.longValue() == -1L ) {
                    this.message = "Не найден клиент по коду МДМ " + instanceCreate.getMdmCode();
                    return 400;
                }

                TppProduct tppExists = tppProductRepo.findFirstByContNumber(instanceCreate.getContractNumber());
                if (tppExists != null) {
                    this.message = "Параметр ContractNumber № договора " + instanceCreate.getContractNumber() + " уже существует для ЭП с ИД " + tppExists.getId();
                    return 400;
                }

                productCodeId = accountRepo.MyProductId( instanceCreate.getProductCode());
                if (productCodeId == null) {
                    this.message = "Ошибка при получении типа продукта по коду " + instanceCreate.getProductCode();
                    return 400;
                }

                tppProduct.setProductCodeId( productCodeId);
                tppProduct.setClientId(clientId);
                tppProduct.setProductType(instanceCreate.getProductType()); //productType.getProductType());
                tppProduct.setContNumber(instanceCreate.getContractNumber());
                tppProduct.setPriority(instanceCreate.getPriority());
                tppProduct.setDateOfConclusion(instanceCreate.getContractDate());
                tppProduct.setPenaltyRate(instanceCreate.getInterestRatePenalty());
                tppProduct.setThresholdAmount(instanceCreate.getThresholdAmount());
                tppProduct.setInterestRateType(instanceCreate.getRateType()); //rateType.ordinal());
                tppProduct.setTaxRate(instanceCreate.getTaxPercentageRate());

                Set<BigInteger> accIdSet = accountRepo.MyAccountId(instanceCreate.getBranchCode(), instanceCreate.getIsoCurrencyCode(),
                                           instanceCreate.getMdmCode(), String.format("%02d", instanceCreate.getPriority()), instanceCreate.getRegisterType());
                bFind = false;
                for (BigInteger accId : accIdSet) {
                    bFind = true;
                    tppProduct.insertRegister(new TppProductRegister(instanceCreate.getRegisterType(), accId, instanceCreate.getIsoCurrencyCode(), "OPEN", ""));
                    break;
                }
                if (!bFind) {
                    this.message = "Ошибка при получении инфо из пула счетов для типа регистра " + instanceCreate.getRegisterType();
                    return 400;
                }
            }
            //доп.соглашения
            for ( Arrangements ar : instanceCreate.getInstanceArrangement() ) {
                Agreement agreementExists = agreementRepo.findFirstByNumber( ar.getNumber() );
                if (agreementExists != null) {
                    this.message = "Параметр № Дополнительного соглашения (сделки) Number " + ar.getNumber() + " уже существует для ЭП с ИД " + agreementExists.getProductId();
                    return 400;
                }
                tppProduct.insertAgreement( new Agreement(
                    ar.generalAgreementId, ar.supplementaryAgreementId, ar.arrangementType, ar.shedulerJobId,
                    ar.number, ar.openingDate, ar.closingDate, ar.cancelDate,
                    ar.validityDuration, ar.cancellationReason, ar.status,
                    ar.interestCalculationDate, ar.interestRate, ar.coefficient, ar.coefficientAction,
                    ar.minimumInterestRate, ar.minimumInterestRateCoefficient, ar.minimumInterestRateCoefficientAction,
                    ar.maximalInterestRate, ar.maximalInterestRateCoefficient, ar.maximalInterestRateCoefficientAction));
            }

            try {
                setInstance( tppProduct);

                InstanceData instanceData = new InstanceData();

                instanceData.setInstanceId( tppProduct.getId().toString());

                for ( Agreement ag : tppProduct.getAgreements())
                    if (ag.isCheckNew())
                        instanceData.setAgrId( ag.getId().toString());

                for ( TppProductRegister re : tppProduct.getRegisters())
                    if (re.isCheckNew())
                        instanceData.setRegId( re.getId().toString() );

                instanceResult.setData( instanceData);

                return 200;
            }
            catch (Exception ex) {
                this.message = ex.getMessage();
                return 500;
            }
        }
        catch (Exception ex) {
            this.message = ex.getMessage();
            return 500;
        }
    }
}
