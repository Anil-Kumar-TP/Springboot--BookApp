package com.anil.BookApp.feedback;

import com.anil.BookApp.book.Book;
import com.anil.BookApp.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Feedback extends BaseEntity {

    private Double note; //like stars 1-5

    private String comment;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

}
