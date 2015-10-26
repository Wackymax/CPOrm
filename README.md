# CPOrm

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-CPOrm-green.svg?style=flat)](https://android-arsenal.com/details/1/2676)

Content Provider ORM for android.  This ORM uses an android sqlite database as a backing store, but interactes with it using  content providers.  You use it like a normal ORM, by creating java objects, and then do all of you interations through the objects. And you get the added benefits of using it like normal content providers, so integration with list views and other components are simple.

Have look at the [Wiki](https://github.com/Wackymax/CPOrm/wiki) for more information.

## Features
1. Supports table creation from the data model.
2. Supports view creation from the data model.
3. Advanced querying capabilities, allowing you to created complex queries easily.
4. It is extendable, allowing you to define custom column type mappings.
5. It is really quick to query, as well as doing bulk inserts. From my own tests you can insert around 5000 objects per second and queries return around 9 000 objects per second.
6. Allows you to add table change listeners for views.
7. Most importantly, it is easy to use.

## Install it

There are four ways to install CPOrm:

#### As a Gradle dependency

This is the preferred way. Simply add:

```groovy
compile 'za.co.cporm:CPOrm:2.96'
```

to your project dependencies and run `gradle build` or `gradle assemble`.

#### As a Maven dependency

Declare the dependency in Maven:

```xml
<dependency>
    <groupId>za.co.cporm</groupId>
    <artifactId>CPOrm</artifactId>
    <version>2.96</version>
</dependency>
```

#### As a library project

Download the source code and import it as a library project in Eclipse. The project is available in the folder **library**. For more information on how to do this, read [here](http://developer.android.com/tools/projects/index.html#LibraryProjects).

#### As a jar

Visit the [releases](https://github.com/Wackymax/CPOrm/releases) page to download jars directly. You can drop them into your `libs` folder and configure the Java build path to include the library. See this [tutorial](http://www.vogella.com/tutorials/AndroidLibraryProjects/article.html) for an excellent guide on how to do this.

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
        public class Author extends QuantumFluxRecord<Author> {
            public String name;
            ...
        }
        ```
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
    QuantumFlux.deleteAll(Book.class);
    Author first = Select.from(Author.class).first();
    first.delete();
    ```

=========================

For detailed configuration and advance usages, go through the [Wiki](https://github.com/Wackymax/CPOrm/wiki)
