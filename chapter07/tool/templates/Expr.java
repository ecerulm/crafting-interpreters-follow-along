package com.craftinginterpreters.lox;

import java.util.List;

abstract class {{ basename }} {
  interface Visitor<R> {
  {% for type in types %}
  {% set typeName = (type | split(":"))[0] %}
    R visit{{typeName}}{{basename}}({{typeName}} {{basename|lower}});
  {% endfor %}
  }

  abstract <R> R accept(Visitor<R> visitor);

  {% for type in types %}
  {% set className = ((type | split(":",1))[0]) %}
  {% set fieldList = (type | split(":",1))[1] %}
  static class {{className}} extends {{basename}} {
     {{className}}({{fieldList}}) {
     {% set fields = fieldList|split(",") %}
     {% for a in fields %}
     {% set fieldName = (a|split(" "))[1] | trim %}
     {% set fieldType = (a|split(" "))[0] | trim %}
         this.{{fieldName}} = {{fieldName}};
     {% endfor %}
     }

     @Override
     <R> R accept(Visitor<R> visitor) {
       return visitor.visit{{className}}{{basename}}(this);
     }

     {% for a in fields %}
     final {{a}};
     {% endfor %}
  }

  {% endfor %}
}
