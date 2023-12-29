package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TEST")
public class Test {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Id
    private Long id;

    @Column(name = "[USER]")  // 使用一个不是保留字的列名
    private String user;

    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", user='" + user + '\'' +
                '}';
    }
}
