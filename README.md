# CPOrm

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-CPOrm-green.svg?style=flat)](https://android-arsenal.com/details/1/2676)
[![](https://jitpack.io/v/Wackymax/CPOrm.svg)](https://jitpack.io/#Wackymax/CPOrm)

A Powerful Content Provider ORM for android.  This ORM uses an android sqlite database as a backing store, but interacts with it using  content providers.  You use it like a normal ORM, by creating java objects, and then do all of you interactions through the objects. And you get the added benefits of using it like normal content providers, so integration with list views and other components are simple.

Have a look at the [Wiki](https://github.com/Wackymax/CPOrm/wiki) for more information.

Please star the repo if you find this library usefull, thank you :)

## Features
1. Supports table creation from the data model.
2. Supports view creation from the data model.
3. Advanced querying capabilities, allowing you to created complex queries easily.
4. It is extendable, allowing you to define custom column type mappings.
5. It is really quick to query, as well as doing bulk inserts. From my own tests you can insert around 5000 objects per second and queries return around 9 000 objects per second.
6. Allows you to add table change listeners for views.
7. Most importantly, it is easy to use.

## Planned Features For Future Release
1. Contract Class Generator
2. Direct DB Access

## Install it

There are four ways to install CPOrm:

#### As a Gradle dependency

This is the preferred way. Simply add:

```groovy
compile 'za.co.cporm:CPOrm:3.0.7'
```

to your project dependencies and run `gradle build` or `gradle assemble`.

#### As a Maven dependency

Declare the dependency in Maven:

```xml
<dependency>
    <groupId>za.co.cporm</groupId>
    <artifactId>CPOrm</artifactId>
    <version>3.0.7</version>
</dependency>
```

#### As a library project

Download the source code and import it as a library project in Eclipse. The project is available in the folder **library**. For more information on how to do this, read [here](http://developer.android.com/tools/projects/index.html#LibraryProjects).

#### As a jar

Visit the [releases](https://github.com/Wackymax/CPOrm/releases) page to download aar directly. You can drop them into your `libs` folder and configure the Java build path to include the library. See this [tutorial](http://www.vogella.com/tutorials/AndroidLibraryProjects/article.html) for an excellent guide on how to do this.


### Proguard

In order to use CPOrm in Android release mode include the following proguard rules

```
# CPORM CONFIGURATION

-keepattributes *Annotation*
-keepclasseswithmembernames class ** {
    @za.co.cporm.model.annotation.* *;
}

-keep class za.co.cporm.** { *; }
-dontwarn za.co.cporm.**
```

===================

## Use it
To use the ORM, include it as a library in you project. Create a configuration that will tell the ORM all the important stuff like the domain model objects, database name etc. You can define a custom SQLColumnMapping factory as well if you want to handle more that the standard java types. 

For more information, check out the example app.

1. Install the project via any of the method above. and extend `Application` class and use it as following:
  
   ```
   public class SampleApplication extends Application {
       @Override
        public void onCreate() {
    
            super.onCreate();
            CPOrm.initialize(this);
        }
   }
   ```
   
2. register this application class in Manifest file:
   
   ```xml
   <application
           android:name=".SampleApplication"
           ...
           >
   ```
   
   
3. add following meta data in `<application>` tag
   
   ```xml
    <meta-data android:name="CPORM_CONFIG" android:value="za.co.cporm.example.model.MyCPOrmConfiguration" />
    <meta-data android:name="MAPPING_FACTORY" android:value="om.cp.orm.example.MyMappingFactory" /><!-- This is optional-->
    <meta-data android:name="AUTHORITY" android:value="za.co.cporm.example" /> <!-- Should match provider-->
   ```
   
   
4. register content provider:
    
    ```xml
    <provider
            android:authorities="za.co.cporm.example" <!-- Should match the Authority Meta Tag-->
            android:name="za.co.cporm.provider.CPOrmContentProvider"
            android:exported="false"
            android:process=":provider" />
    ```
    
5. Now, you just have to create model classes by:
    * annotation:
        
        ```
        @Table
        public class Book {
        ...
        }
        ```
    
    * extending class:
        
        ```
        @Table
        public class Author extends CPDefaultRecord<Author> {
            @Column
            public String name;
            ...
        }
        ```
        
    #####Notes:

    * When you extend CPDefaultRecord you have access to all default access methods such as save(), update(), delete(), etc. and If you are using just @Table annotation, you can use those methods from CPOrm class. for e.g. CPOrm.insert(authorObject);.
    
    * When you extend CPDefaultRecord you have already a auto increment primary key with name '_id'. So while using just @Table annotation, consider creating a primary key.
    
    * Name of columns and tables will convert from camelCasing to under_score. for e.g. field bookName in a model will become book_name in database.
    
    #####Column Constraint annotations:

    * `@Column` will allow to change name of column and allow to make it NOT NULL. Default values for not null is false. for e.g. 
    
    ```
    @Column(columnName = "cover_name")
    private String bookName;
    ``` 
    
    ```
    @Column(required = true)
    private String isbn;
    ```
    
    * `@PrimaryKey` will allow to make a column as primary key, and auto increment. Default value for autoIncrement is true.  For now the library only supports keys of type long. for e.g. 
    
    ```
    @PrimaryKey
    private String isbn;
    ```
    
    ```
    @PrimaryKey(autoIncrement = false)
    private String isbn;
    ```
    
    * `@Unique` will allow you to make a column value unique.

6. Register your model classes
  ```
  public class MyCPOrmConfiguration implements CPOrmConfiguration {

        @Override
        public String getDatabaseName() {
    
            return "example.db";
        }
    
        @Override
        public int getDatabaseVersion() {
    
            return 1;
        }
    
        @Override
        public boolean isQueryLoggingEnabled() {
    
            return false;
        }
    
        @Override
        public List<Class<?>> getDataModelObjects() {
            List<Class<?>> domainObjects = new ArrayList<Class<?>>();
            domainObjects.add(Book.class);
            domainObjects.add(Author.class);
    
            return domainObjects;
        }
    }
  ```
7. Access:
    
    ```
    Book book = new Book();
    book.name = "Sorcerer's Stone";
    book.isbn = "122342564";
    CPOrm.insert(book);
    ```
    
    ```
    Author author = new Author();
    author.name = "J.K. Rollings";
    author.save();
    ```
    
    ```
    Author first = Select.from(Author.class).first();
    first.name = "J. K. Rowling";
    first.update();
    ```
    
    ```
    CPOrm.deleteAll(Book.class);
    Author first = Select.from(Author.class).first();
    first.delete();
    ```

=========================

For detailed configuration and advance usages, go through the [Wiki](https://github.com/Wackymax/CPOrm/wiki)
