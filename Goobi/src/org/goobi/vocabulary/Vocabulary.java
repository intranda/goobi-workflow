package org.goobi.vocabulary;

import java.util.List;
import lombok.Data;

@Data
public class Vocabulary {
	private String title;
	private String description;
	private List<VocabRecord> records;
}
