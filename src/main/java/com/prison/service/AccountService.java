package com.prison.service;

import com.prison.dto.RemittanceDTO;
import com.prison.entity.Inmate;
import com.prison.entity.InmateAccount;
import com.prison.entity.RemittanceRecord;
import com.prison.entity.TemporaryLimitAdjustment;
import com.prison.repository.InmateAccountRepository;
import com.prison.repository.InmateRepository;
import com.prison.repository.RemittanceRecordRepository;
import com.prison.repository.TemporaryLimitAdjustmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final InmateAccountRepository inmateAccountRepository;
    private final RemittanceRecordRepository remittanceRecordRepository;
    private final TemporaryLimitAdjustmentRepository temporaryLimitAdjustmentRepository;
    private final InmateRepository inmateRepository;

    @Transactional
    public InmateAccount createAccount(Long inmateId) {
        if (inmateAccountRepository.findByInmateId(inmateId).isPresent()) {
            throw new RuntimeException("该服刑人员账户已存在");
        }
        Inmate inmate = inmateRepository.findById(inmateId)
                .orElseThrow(() -> new RuntimeException("服刑人员不存在"));
        InmateAccount account = new InmateAccount();
        account.setInmateId(inmateId);
        account.setAccountNo(generateAccountNo());
        account.setBalance(BigDecimal.ZERO);
        account.setMonthlyLimit(new BigDecimal("2000"));
        account.setStatus("正常");
        return inmateAccountRepository.save(account);
    }

    public InmateAccount getAccountByInmateId(Long inmateId) {
        return inmateAccountRepository.findByInmateId(inmateId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
    }

    public InmateAccount getAccountById(Long id) {
        return inmateAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
    }

    public Page<InmateAccount> listAccounts(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return inmateAccountRepository.findByStatus(status, pageable);
        }
        return inmateAccountRepository.findAll(pageable);
    }

    @Transactional
    public InmateAccount updateMonthlyLimit(Long inmateId, BigDecimal newLimit) {
        InmateAccount account = inmateAccountRepository.findByInmateId(inmateId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        if (newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("月消费限额不能为负数");
        }
        account.setMonthlyLimit(newLimit);
        return inmateAccountRepository.save(account);
    }

    @Transactional
    public InmateAccount updateAccountStatus(Long inmateId, String status) {
        InmateAccount account = inmateAccountRepository.findByInmateId(inmateId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        account.setStatus(status);
        return inmateAccountRepository.save(account);
    }

    @Transactional
    public RemittanceRecord addRemittance(RemittanceDTO dto) {
        InmateAccount account = inmateAccountRepository.findByInmateId(dto.getInmateId())
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        if (!"正常".equals(account.getStatus())) {
            throw new RuntimeException("账户状态异常，无法汇款");
        }
        RemittanceRecord record = new RemittanceRecord();
        record.setInmateId(dto.getInmateId());
        record.setRemitterName(dto.getRemitterName());
        record.setRelationship(dto.getRelationship());
        record.setAmount(dto.getAmount());
        record.setRemittanceDate(LocalDate.parse(dto.getRemittanceDate()));
        record.setSourceType(dto.getSourceType() != null ? dto.getSourceType() : "家属汇款");
        record.setRemark(dto.getRemark());
        record.setStatus("已到账");
        remittanceRecordRepository.save(record);
        account.setBalance(account.getBalance().add(dto.getAmount()));
        inmateAccountRepository.save(account);
        return record;
    }

    public Page<RemittanceRecord> listRemittances(Long inmateId, String status, Pageable pageable) {
        if (inmateId != null) {
            return remittanceRecordRepository.findByInmateId(inmateId, pageable);
        }
        if (status != null && !status.isBlank()) {
            return remittanceRecordRepository.findByStatus(status, pageable);
        }
        return remittanceRecordRepository.findAll(pageable);
    }

    public BigDecimal getEffectiveMonthlyLimit(Long inmateId) {
        InmateAccount account = inmateAccountRepository.findByInmateId(inmateId)
                .orElseThrow(() -> new RuntimeException("账户不存在"));
        BigDecimal baseLimit = account.getMonthlyLimit();
        LocalDate today = LocalDate.now();
        List<TemporaryLimitAdjustment> activeAdjustments =
                temporaryLimitAdjustmentRepository.findActiveAdjustments(inmateId, today);
        for (TemporaryLimitAdjustment adjustment : activeAdjustments) {
            if (adjustment.getAdjustedLimit().compareTo(baseLimit) > 0) {
                baseLimit = adjustment.getAdjustedLimit();
            }
        }
        return baseLimit;
    }

    public Map<String, Object> getMonthlyLimitInfo(Long inmateId) {
        Map<String, Object> result = new HashMap<>();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        BigDecimal effectiveLimit = getEffectiveMonthlyLimit(inmateId);
        result.put("effectiveLimit", effectiveLimit);
        result.put("baseLimit", getAccountByInmateId(inmateId).getMonthlyLimit());
        result.put("month", currentMonth.toString());
        return result;
    }

    private String generateAccountNo() {
        return "ACC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public List<Object[]> getBalanceDistribution() {
        return inmateAccountRepository.getBalanceDistribution();
    }
}
