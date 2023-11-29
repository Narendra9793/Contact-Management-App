package com.contactmanager.contactmanager.Dao;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.contactmanager.contactmanager.Models.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer>{

    @Query("FROM Contact as c WHERE c.user.id = :id")
    public Page<Contact> findContactById(@Param("id") int id, Pageable pageable);
    
}
