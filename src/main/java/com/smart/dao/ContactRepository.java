package com.smart.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; 

import com.smart.entities.Contact;
import com.smart.entities.User;

import java.util.List;

public interface ContactRepository  extends JpaRepository<Contact, Integer> {
	
	@Query("from Contact c where c.user.id=:id")
	public Page<Contact> findContactsByUser(@Param("id") int id, Pageable pageable);
	
	public List<Contact> findByNameContainingAndUser(String keywords,User user);
}
