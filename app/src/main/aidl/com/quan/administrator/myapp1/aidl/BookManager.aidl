// BookManager.aidl
package com.quan.administrator.myapp1.aidl;
// Declare any non-default types here with import statements
import com.quan.administrator.myapp1.aidl.Book;
import com.quan.administrator.myapp1.aidl.IOnNewBookArrivedListener;

interface BookManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    List<Book> getBookList();
    void addBook(in Book book);

    void  registerBookArrivedListener(IOnNewBookArrivedListener listener);
    void  unRegisterBookArrivedListener(IOnNewBookArrivedListener listener);
}
