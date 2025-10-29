package cloud.xcan.angus.plugin.jpa;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plugins")
public class PluginEntity {
    @Id
    private String id;
    private String name;
    private String version;
    @Lob
    @Column(name = "data", columnDefinition = "BLOB")
    private byte[] data;
    private LocalDateTime uploadedAt;

    public PluginEntity() {
    }

    public PluginEntity(String id, String name, String version, byte[] data, LocalDateTime uploadedAt) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.data = data;
        this.uploadedAt = uploadedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}

