package com.hospital.dao;

import com.hospital.model.PatientNote;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MongoNoteDAO {

    private static final String CONFIG_FILE = "src/config.properties";
    private static final String DB_NAME = "hospital_db";
    private static final String COLLECTION_NAME = "patient_notes";
    private String connectionString;

    private MongoCollection<Document> collection;

    public MongoNoteDAO() {
        loadConfig();
        // Only attempt connection if string is configured
        if (connectionString == null || connectionString.isEmpty() || connectionString.contains("<password>")) {
            System.out.println("⚠️ MongoDB connection string not configured. Patient notes will not be saved.");
            return;
        }

        try {
            ConnectionString connString = new ConnectionString(connectionString);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .build();
            MongoClient mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase(DB_NAME);
            collection = database.getCollection(COLLECTION_NAME);
            System.out.println("✅ Connected to MongoDB Atlas successfully.");
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            this.connectionString = props.getProperty("mongodb.uri");
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Could not load config.properties for MongoDB.");
        }
    }

    public void addNote(PatientNote note) {
        if (collection == null)
            return;

        Document doc = new Document("_id", note.getId())
                .append("patientId", note.getPatientId())
                .append("category", note.getCategory())
                .append("createdAt", note.getCreatedAt().toString()); // Simple string storage for date

        // Add dynamic content
        if (note.getContent() != null) {
            Document contentDoc = new Document();
            for (Map.Entry<String, String> entry : note.getContent().entrySet()) {
                contentDoc.append(entry.getKey(), entry.getValue());
            }
            doc.append("content", contentDoc);
        }

        collection.insertOne(doc);
    }

    public List<PatientNote> getNotesByPatientId(int patientId) {
        List<PatientNote> notes = new ArrayList<>();
        if (collection == null)
            return notes;

        // Filter by patientId
        Document query = new Document("patientId", patientId);

        for (Document doc : collection.find(query)) {
            PatientNote note = new PatientNote();
            note.setId(doc.getString("_id"));
            note.setPatientId(doc.getInteger("patientId"));
            note.setCategory(doc.getString("category"));

            String dateStr = doc.getString("createdAt");
            if (dateStr != null) {
                note.setCreatedAt(java.time.LocalDateTime.parse(dateStr));
            }

            // Extract dynamic content
            Document contentDoc = (Document) doc.get("content");
            if (contentDoc != null) {
                java.util.Map<String, String> contentMap = new java.util.HashMap<>();
                for (String key : contentDoc.keySet()) {
                    contentMap.put(key, contentDoc.getString(key));
                }
                note.setContent(contentMap);
            }
            notes.add(note);
        }
        return notes;
    }
}
