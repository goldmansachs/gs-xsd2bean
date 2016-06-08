# Xsd2bean - A dependency-free XML to Object Mapper

Xsd2bean is an XML to object mapper.
Given an xsd, it will generate java code that represents the model described in the xsd
with the ability to read an xml and create the java objects and vice versa (unmarshall/marshall).

### Xsd2bean Principles
* Xsd is the master model.
  * The model is defined by the xsd (and therefore, there is no duplication).
  * The xsd is owned by the team and they are the sole consumers of conforming xmls.
  * This defines the sweet spot for Xsd2bean and narrows its applicability scope.
  * The mapping is fixed (and therefore never needs to be defined).
  * No transformations are allowed. Any change to the model must be made in the xsd.
* Concrete classes are owned by the team; their abstract parent is generated.
* Zero runtime dependency
* Broad compatibility
  * Works on any veriosn of JDK 1.6+
  * If library A uses Xsd2bean version X and library B uses Xsd2bean version Y, there is never any dependency clash at runtime.
* Parsing is strict. Only elements defined in the xsd are allowed.
  * Xsd2bean should not be used when the parsing needs to be lenient or best-effort.

### Xsd2bean implementation
* Use the same principles as YACC/BISON: generate code that's self contained.
  * Everything that the generated java code relies on is either in the generated output or in the JDK (1.6+)
  * Don't even need xsd2bean.jar on the classpath at runtime!
* Use the fastest XML parsing technology (StAX ' Streaming API for XML)

### Caveats
* Can't deal with arbitrary xsd's.
  * The XSD spec is very large and it will be a long time before we're fully spec compliant
* Less common things not yet implemented:
  * Various built in data types
  * Namespaces
  * Includes

### Using Xsd2bean
* Generate the code starting from an xsd.
  * The generator can be invoked as java main class, from an ant task or programatically
* Creates two special classes: FooUnmarshaller, FooMarshaller
  * With methods to parse and marshal, respectively.
* The domain is generated as abstract classes with concrete classes that you own.