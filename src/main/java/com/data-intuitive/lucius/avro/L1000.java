/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.data-intuitive.lucius.avro;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface L1000 {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"L1000\",\"namespace\":\"com.data-intuitive.lucius.avro\",\"types\":[{\"type\":\"record\",\"name\":\"Compound\",\"fields\":[{\"name\":\"jnjs\",\"type\":\"int\"},{\"name\":\"jnjb\",\"type\":\"int\"},{\"name\":\"smiles\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"inchikey\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"compoundName\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"compoundType\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"compoundTargets\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}}]}],\"messages\":{}}");

  @SuppressWarnings("all")
  public interface Callback extends L1000 {
    public static final org.apache.avro.Protocol PROTOCOL = com.data-intuitive.lucius.avro.L1000.PROTOCOL;
  }
}