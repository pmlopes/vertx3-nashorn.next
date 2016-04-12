package com.jetdrone.nashorn.next;

import jdk.nashorn.api.scripting.JSObject;

import java.util.Collection;
import java.util.Set;

public class JSModule implements JSObject {

  // TODO: add properties
  // exports [object]
  // children [array]
  // id [string]
  // filename [string]
  // loaded [bool]
  // parent [object]

  @Override
  public Object call(Object o, Object... objects) {
    return null;
  }

  @Override
  public Object newObject(Object... objects) {
    return null;
  }

  @Override
  public Object eval(String s) {
    return null;
  }

  @Override
  public Object getMember(String s) {
    return null;
  }

  @Override
  public Object getSlot(int i) {
    return null;
  }

  @Override
  public boolean hasMember(String s) {
    return false;
  }

  @Override
  public boolean hasSlot(int i) {
    return false;
  }

  @Override
  public void removeMember(String s) {

  }

  @Override
  public void setMember(String s, Object o) {

  }

  @Override
  public void setSlot(int i, Object o) {

  }

  @Override
  public Set<String> keySet() {
    return null;
  }

  @Override
  public Collection<Object> values() {
    return null;
  }

  @Override
  public boolean isInstance(Object o) {
    return false;
  }

  @Override
  public boolean isInstanceOf(Object o) {
    return false;
  }

  @Override
  public String getClassName() {
    return null;
  }

  @Override
  public boolean isFunction() {
    return false;
  }

  @Override
  public boolean isStrictFunction() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public double toNumber() {
    return 0;
  }
}
