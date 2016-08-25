package org.camunda.bpm.hackdays.prediction;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;

public class RootMapELResolver extends ELResolver {

  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    return Object.class;
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    return null;
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    return Object.class;
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    Map<String, Object> map = (Map<String, Object>) context.getContext(Map.class);
    
    if (map != null && base == null) {
      if (!map.containsKey(property)) {
        return null;
      }
      else {
        context.setPropertyResolved(true);
        return map.get(property);
      }
    }
    return null;
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
  }

}
