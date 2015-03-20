# CPOrm
Content Provider ORM for android.  This ORM uses an android sqlite database as a backing store, but interactes with it using  content providers.  You use it like a normal ORM, by creating java objects, and then do all of you interations through the objects. And you get the added benefits of using it like normal content providers, so integration with list views and other components are simple.

# Features
1. Supports table creation from the data model.
2. Supports view creation from the data model.
3. Advanced querying capabilities, allowing you to created complex queries easily.
4. It is extendable, allowing you to define custom column type mappings.
5. Most importantly, it is easy to use.

#Using it
To use the ORM, include it as a library in you project.  Create a Model Factory class by implementing ModelFactory, this will tell the ORM which classes belong to the model.  You can define a custom SQLColumnMapping factory as well if you want to handle more that the standard java types. Now all that is left to do is define the meta tags that will tell the ORM all the important stuff. Add these as part of the application element in the Android Manifest


      <meta-data android:name="DATABASE" android:value="example.db" />
      <meta-data android:name="VERSION" android:value="1" /> <!-- Increase this to have the db recreated with new model changes -->
      <meta-data android:name="QUERY_LOG" android:value="true" />
      <meta-data android:name="MODEL_FACTORY" android:value="om.cp.orm.example.MyModelFactory" />
      <meta-data android:name="MAPPING_FACTORY" android:value="om.cp.orm.example.MyMappingFactory" /><!-- This is optional-->
      <meta-data android:name="AUTHORITY" android:value="om.cp.orm.example" /> <!-- Should match provider-->
      
      <provider
            android:authorities="za.co.cporm.example"
            android:name="za.co.cporm.provider.CPOrmContentProvider"
            android:exported="false"
            android:permission="true"/>

Now that the setup is done, all you need to do is create your model objects, stick on some annotations, link them to the model factory, and you are done.  Some notable annotations are Table, Column, and Primary Key.

To interact with the ORM you can extend the CPRecord class, this will make it easy to do CRUD operations, or you can use the CPHelper class if you can't extend CPRecord. To perform queries use the Select class.  To take advantage of the built in support for content providers on android, extends the CPDefaultRecord class, this already has a _id column defined.

For more information, check out the example app.
