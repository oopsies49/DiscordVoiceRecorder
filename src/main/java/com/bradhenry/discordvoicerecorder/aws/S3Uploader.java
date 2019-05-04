package com.bradhenry.discordvoicerecorder.aws;

import com.bradhenry.discordvoicerecorder.DiscordVoiceRecorderProperties;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

public class S3Uploader {
    private final DiscordVoiceRecorderProperties properties;

    public S3Uploader(DiscordVoiceRecorderProperties properties) {
        this.properties = properties;
    }

    public String uploadFile(File file) {
        S3Client client = S3Client.builder().build();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getAwsBucket())
                .key(file.getName())
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
        client.putObject(putObjectRequest, file.toPath());
        return getURL(properties.getAwsBucket(), file.getName());
    }

    private String getURL(String bucket, String key) {
        // https://bucket.s3.amazonaws.com/key
        return String.format("https://%s.s3.amazonaws.com/%s", bucket, key);
    }
}
