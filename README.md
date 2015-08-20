# CPOrm
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
compile 'za.co.cporm:CPOrm:2.94'
```

to your project dependencies and run `gradle build` or `gradle assemble`.

#### As a Maven dependency

Declare the dependency in Maven:

```xml
<dependency>
    <groupId>za.co.cporm</groupId>
    <artifactId>CPOrm</artifactId>
    <version>2.94</version>
</dependency>
```

#### As a library project

Download the source code and import it as a library project in Eclipse. The project is available in the folder **library**. For more information on how to do this, read [here](http://developer.android.com/tools/projects/index.html#LibraryProjects).

#### As a jar

Visit the [releases](https://github.com/Wackymax/CPOrm/releases) page to download jars directly. You can drop them into your `libs` folder and configure the Java build path to include the library. See this [tutorial](http://www.vogella.com/tutorials/AndroidLibraryProjects/article.html) for an excellent guide on how to do this.

===================

## Use it
To use the ORM, include it as a library in you project. Create a configuration that will tell the ORM all the important stuff like the domain model objects, database name etc. You can define a custom SQLColumnMapping factory as well if you want to handle more that the standard java types. Now all that is left to do is define the meta tags that will tell the ORM all the important stuff. Add these as part of the application element in the Android Manifest

```xml
      <meta-data android:name="CPORM_CONFIG" android:value="za.co.cporm.example.model.MyCPOrmConfiguration" />
      <meta-data android:name="MAPPING_FACTORY" android:value="om.cp.orm.example.MyMappingFactory" /><!-- This is optional-->
      <meta-data android:name="AUTHORITY" android:value="om.cp.orm.example" /> <!-- Should match provider-->
      
      <provider
            android:authorities="za.co.cporm.example"
            android:name="za.co.cporm.provider.CPOrmContentProvider"
            android:exported="false"
            android:permission="true"/>
```
Now that the setup is done, all you need to do is create your model objects, stick on some annotations, link them to the model factory, and you are done.  Some notable annotations are Table, Column, and Primary Key.

To interact with the ORM you can extend the CPRecord class, this will make it easy to do CRUD operations, or you can use the CPHelper class if you can't extend CPRecord. To perform queries use the Select class.  To take advantage of the built in support for content providers on android, extend the CPDefaultRecord class, this already has a _id column defined, as well as some usefull methods to interact with the objects.

For more information, check out the example app.
