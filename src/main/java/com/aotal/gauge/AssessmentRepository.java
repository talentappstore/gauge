package com.aotal.gauge;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface AssessmentRepository extends CrudRepository<Assessment, Long> {

    Assessment findByKey(long key);
//    List<Assessment> findByAssessmentID(long id);
}
