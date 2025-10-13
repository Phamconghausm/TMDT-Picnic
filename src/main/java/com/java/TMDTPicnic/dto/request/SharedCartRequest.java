package com.java.TMDTPicnic.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.java.TMDTPicnic.entity.User;
import com.java.TMDTPicnic.enums.SharedCartStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

public class SharedCartRequest {
    private String title;
    private SharedCartStatus status;
}
