package org.hsdd.repo;

import org.hsdd.model.IssueReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueReportRepository extends JpaRepository<IssueReport, Long> {

    List<IssueReport> findByPredictionId(Long predictionId);
}
