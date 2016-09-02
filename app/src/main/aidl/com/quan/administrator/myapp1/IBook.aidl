// IBook.aidl
package com.quan.administrator.myapp1;

// Declare any non-default types here with import statements

interface IBook {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
   void addBook();
   void delBook(int id);
}
