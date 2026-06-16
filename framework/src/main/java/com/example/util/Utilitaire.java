package com.example.util;

import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

    /**
    * This class is intended to provide utility methods for the framework, such as:
    * - Scanning for classes with specific annotations (e.g., @Controller)
    * - Instantiating classes and managing dependencies
    * - Handling configuration and properties
    */
public class Utilitaire {
    /**
     * Scanne un package et retourne la liste des noms de classes annotées par @Controller
     */
    public static List<String> scanControllers(String packageToScan) {
        ClassGraph cg = new ClassGraph().enableClassInfo().enableAnnotationInfo();
        
        if (packageToScan != null && !packageToScan.isBlank()) {
            cg.acceptPackages(packageToScan);
        }

        try (ScanResult scanResult = cg.scan()) {
            // On récupère toutes les classes qui portent notre annotation spécifique
            return scanResult
                    .getClassesWithAnnotation("com.example.servlet.Controller")
                    .getNames(); // Retourne une List<String> des noms de classes
        }
    }
}
