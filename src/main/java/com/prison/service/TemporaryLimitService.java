package com.prison.service;

import com.prison.dto.TemporaryLimitDTO;
import com.prison.entity.InmateAccount;
import com.prison.entity.TemporaryLimitAdjustment;
import com.prison.repository.InmateAccountRepository;
import com.prison.repository.TemporaryLimitAdjustmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemporaryLimitService {

    private final TemporaryLimitAdjustmentRepository temporaryLimitAdjustmentRepository;
    private final InmateAccountRepository inmateAccountRepository;

    @Transactional
    public TemporaryLimitAdjustment createTemporaryLimit(TemporaryLimitDTO dto) {
        InmateAccount account = inmateAccountRepository.findByInmateId(dto.getInmateId())
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        LocalDate effectiveDate = LocalDate.parse(dto.getEffectiveDate());
        LocalDate expiryDate = LocalDate.parse(dto.getExpiryDate());

        if (expiryDate.isBefore(effectiveDate)) {
            throw new RuntimeException("失效日期不能早于生效日期");
        }

        if (dto.getAdjustedLimit().compareTo(account.getMonthlyLimit()) <= 0) {
            throw new RuntimeException("临时提额必须大于基础额度");
        }

        TemporaryLimitAdjustment adjustment = new TemporaryLimitAdjustment();
        adjustment.setInmateId(dto.getInmateId());
        adjustment.setOriginalLimit(account.getMonthlyLimit());
        adjustment.setAdjustedLimit(dto.getAdjustedLimit());
        adjustment.setEffectiveDate(effectiveDate);
        adjustment.setExpiryDate(expiryDate);
        adjustment.setReason(dto.getReason());
        adjustment.setStatus("待审批");
        adjustment.setAppliedBy(dto.getAppliedBy());

        return temporaryLimitAdjustmentRepository.save(adjustment);
    }

    @Transactional
    public TemporaryLimitAdjustment approveTemporaryLimit(Long id, Long approverId, boolean approved, String rejectReason) {
        TemporaryLimitAdjustment adjustment = temporaryLimitAdjustmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("临时提额申请不存在"));

        if (!"待审批".equals(adjustment.getStatus())) {
            throw new RuntimeException("申请状态不允许审批");
        }

        adjustment.setApprovedBy(approverId);
        adjustment.setApprovedAt(LocalDateTime.now());

        if (approved) {
            adjustment.setStatus("已通过");
        } else {
            adjustment.setStatus("已拒绝");
            adjustment.setRejectReason(rejectReason);
        }

        return temporaryLimitAdjustmentRepository.save(adjustment);
    }

    public TemporaryLimitAdjustment getTemporaryLimitById(Long id) {
        return temporaryLimitAdjustmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("临时提额申请不存在"));
    }

    public Page<TemporaryLimitAdjustment> listTemporaryLimits(Long inmateId, String status, Pageable pageable) {
        if (inmateId != null) {
            return temporaryLimitAdjustmentRepository.findByInmateId(inmateId, pageable);
        }
        if (status != null && !status.isBlank()) {
            return temporaryLimitAdjustmentRepository.findByStatus(status, pageable);
        }
        return temporaryLimitAdjustmentRepository.findAll(pageable);
    }

    public List<TemporaryLimitAdjustment> getActiveAdjustments(Long inmateId) {
        return temporaryLimitAdjustmentRepository.findActiveAdjustments(inmateId, LocalDate.now());
    }

    public long countPendingApproval() {
        return temporaryLimitAdjustmentRepository.countByStatus("待审批");
    }
}
