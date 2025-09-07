package com.desarrollo.raffy.business.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.desarrollo.raffy.model.Events;

@Repository
public interface EventsRepository extends CrudRepository<Events, Long> {

}