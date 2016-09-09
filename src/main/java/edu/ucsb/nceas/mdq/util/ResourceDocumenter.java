package edu.ucsb.nceas.mdq.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.ws.rs.Path;

import edu.ucsb.nceas.mdq.rest.ChecksResource;

public class ResourceDocumenter {
	
	public static String inspectClass(Class clazz) {
		StringBuffer sb = new StringBuffer();
		sb.append("Class: " + clazz.getName());
		sb.append("\n");
		
		Annotation path = clazz.getAnnotation(Path.class);
		sb.append("Path: " + path.toString());
		sb.append("\n");


		Method[] methods = clazz.getMethods();
		for (Method method: methods) {
			if (method.getAnnotations() == null || method.getAnnotations().length == 0) {
				continue;
			}
			sb.append("\t");
			sb.append("Method: " + method.getName());
			sb.append("\n");


			// loop through the method annotations
			Annotation[] annotations = method.getAnnotations();
			for (Annotation a: annotations) {
				sb.append("\t");
				sb.append("\t");
				sb.append("Annotation: " + a.toString());
				sb.append("\n");

			}
			
			// loop through the parameter annotations
			for (Parameter parameter: method.getParameters()) {
				sb.append("\t");
				sb.append("\t");
				sb.append("Parameter: " + parameter.getName());
				sb.append("\n");

				for (Annotation a: parameter.getAnnotations()) {
					sb.append("\t");
					sb.append("\t");
					sb.append("\t");
					sb.append("Parameter Annotation: " + a.toString());
					sb.append("\n");

				}
			}
			
		}
		return sb.toString();
		
	}
	
	public static void main(String[] args) {
		String info = ResourceDocumenter.inspectClass(ChecksResource.class);
		System.out.println(info);
		
	}

}
