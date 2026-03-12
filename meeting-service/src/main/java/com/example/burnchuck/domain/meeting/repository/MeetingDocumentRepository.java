package com.example.burnchuck.domain.meeting.repository;

import com.example.burnchuck.domain.document.MeetingDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MeetingDocumentRepository extends ElasticsearchRepository<MeetingDocument, Long> {

}
