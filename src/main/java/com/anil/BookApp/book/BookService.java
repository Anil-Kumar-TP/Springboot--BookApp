package com.anil.BookApp.book;

import com.anil.BookApp.common.PageResponse;
import com.anil.BookApp.exceptions.OperationNotPermittedException;
import com.anil.BookApp.file.FileStorageService;
import com.anil.BookApp.history.BookTransactionHistory;
import com.anil.BookApp.history.BookTransactionHistoryRepository;
import com.anil.BookApp.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService {

    private final  BookMapper bookMapper;
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private final FileStorageService fileStorageService;

    public Integer save(BookRequest request, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Book book = bookMapper.toBook(request);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId).map(bookMapper::toBookResponse).orElseThrow(()->new EntityNotFoundException("No book found with the Id: " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable,user.getId());
        List<BookResponse> bookResponse = books.stream().map(bookMapper::toBookResponse).toList();
        return new PageResponse<>(bookResponse, books.getNumber(),books.getSize(),books.getTotalElements(),books.getTotalPages(),books.isFirst(),books.isLast());
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAll(BookSpecification.withOwnerId(user.getId()),pageable);
        List<BookResponse> bookResponse = books.stream().map(bookMapper::toBookResponse).toList();
        return new PageResponse<>(bookResponse, books.getNumber(),books.getSize(),books.getTotalElements(),books.getTotalPages(),books.isFirst(),books.isLast());
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllBorrowedBooks(pageable,user.getId());
        List<BorrowedBookResponse> borrowedBookResponse = allBorrowedBooks.stream().map(bookMapper::toBorrowedBookResponse).toList();
        return new PageResponse<>(borrowedBookResponse, allBorrowedBooks.getNumber(),allBorrowedBooks.getSize(),allBorrowedBooks.getTotalElements(),allBorrowedBooks.getTotalPages(),allBorrowedBooks.isFirst(),allBorrowedBooks.isLast());
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = bookTransactionHistoryRepository.findAllReturnedBooks(pageable,user.getId());
        List<BorrowedBookResponse> borrowedBookResponse = allBorrowedBooks.stream().map(bookMapper::toBorrowedBookResponse).toList();
        return new PageResponse<>(borrowedBookResponse, allBorrowedBooks.getNumber(),allBorrowedBooks.getSize(),allBorrowedBooks.getTotalElements(),allBorrowedBooks.getTotalPages(),allBorrowedBooks.isFirst(),allBorrowedBooks.isLast());
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new EntityNotFoundException("No book found with Id: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermittedException("You cannot update status of sharing books of other people");
        }
        book.setShareable(!book.isShareable());
        bookRepository.save(book);
        return bookId;
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new EntityNotFoundException("No book found with Id: " + bookId));
        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermittedException("You cannot update status of archiving books of other people");
        }
        book.setArchived(!book.isArchived());
        bookRepository.save(book);
        return bookId;
    }

    //only check for the loggedIn user borrowing the book.In real world,u should do if there is only a single copy of a book then that book must be returned , not limited to loggedIn user.
    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new EntityNotFoundException("Mo book found with Id :" + bookId));
        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("You cannot borrow archived / non-shareable books");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermittedException("You cannot borrow your own book");
        }
        final boolean isAlreadyBorrowed = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(bookId,user.getId());
        if (isAlreadyBorrowed){
            throw new OperationNotPermittedException("Book was already borrowed");
        }
        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder().
                                                        user(user).
                                                        book(book).
                                                        returned(false).
                                                        returnApproved(false).
                                                        build();
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new EntityNotFoundException("Mo book found with Id :" + bookId));
        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("You cannot borrow archived / non-shareable books");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndUserId(book,user.getId()).orElseThrow(()->new OperationNotPermittedException("You did not borrowed this book to return"));
        bookTransactionHistory.setReturned(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnedBorrowedBook(Integer bookId, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new EntityNotFoundException("Mo book found with Id :" + bookId));
        if (book.isArchived() || !book.isShareable()){
            throw new OperationNotPermittedException("You cannot borrow archived / non-shareable books");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }
        BookTransactionHistory bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndOwnerId(book,user.getId()).orElseThrow(()->new OperationNotPermittedException("Book is not returned yet,so u cannot approve it"));
        bookTransactionHistory.setReturnApproved(true);
        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public void uploadBookCoverPicture(Integer bookId, MultipartFile file, Authentication connectedUser) {
        Book book = bookRepository.findById(bookId).orElseThrow(()->new EntityNotFoundException("Mo book found with Id :" + bookId));
        User user = ((User) connectedUser.getPrincipal());
        var bookCover = fileStorageService.saveFile(file,user.getId());
        book.setBookCover(bookCover);
        bookRepository.save(book);
    }
}
