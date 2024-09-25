package com.echall.platform.content.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.echall.platform.content.domain.entity.ContentDocument;

@Repository
public interface ContentScriptRepository extends MongoRepository<ContentDocument, ObjectId> {

	Optional<ContentDocument> findById(ObjectId contentId);
	Optional<ContentDocument> findByScriptList(List<String> script);
	ContentDocument findContentDocumentById(ObjectId mongoContentId);
}