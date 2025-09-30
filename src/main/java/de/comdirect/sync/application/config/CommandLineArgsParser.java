package de.comdirect.sync.application.config;

import de.comdirect.sync.domain.valueobjects.DateRange;
import de.comdirect.sync.infrastructure.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser para argumentos de línea de comandos relacionados con la descarga de documentos.
 * 
 * Soporta los siguientes argumentos:
 * --init-date=YYYY-MM-DD : Fecha inicial para la descarga
 * --end-date=YYYY-MM-DD  : Fecha final para la descarga
 * --help                 : Muestra la ayuda
 * 
 * Ejemplos de uso:
 * java -jar app.jar --init-date=2025-06-01 --end-date=2025-09-20
 * java -jar app.jar --init-date=2025-06-01  (end-date = hoy)
 * java -jar app.jar                         (mes actual)
 */
@Component
public class CommandLineArgsParser {
    
    private static final Logger log = LoggerFactory.getLogger(CommandLineArgsParser.class);
    private final MessageService messageService;
    
    private static final String INIT_DATE_ARG = "--init-date";
    private static final String END_DATE_ARG = "--end-date";
    private static final String HELP_ARG = "--help";
    
    public CommandLineArgsParser(MessageService messageService) {
        this.messageService = messageService;
    }
    
    private final Map<String, String> parsedArgs = new HashMap<>();
    private boolean helpRequested = false;
    
    /**
     * Parsea los argumentos de la línea de comandos
     */
    public void parseArgs(String[] args) {
        log.info(messageService.getMessage("cli.args.parsing", Arrays.toString(args)));
        
        for (String arg : args) {
            if (arg.equals(HELP_ARG)) {
                helpRequested = true;
                continue;
            }
            
            if (arg.startsWith(INIT_DATE_ARG + "=")) {
                String dateValue = arg.substring((INIT_DATE_ARG + "=").length());
                parsedArgs.put(INIT_DATE_ARG, dateValue);
                log.debug("Fecha inicial parseada: {}", dateValue);
            } else if (arg.startsWith(END_DATE_ARG + "=")) {
                String dateValue = arg.substring((END_DATE_ARG + "=").length());
                parsedArgs.put(END_DATE_ARG, dateValue);
                log.debug("Fecha final parseada: {}", dateValue);
            } else if (arg.startsWith("--")) {
                log.warn(messageService.getMessage("cli.args.unknown.ignored", arg));
            }
        }
    }
    
    /**
     * Crea un DateRange basado en los argumentos parseados
     */
    public DateRange createDateRange() {
        String initDateStr = parsedArgs.get(INIT_DATE_ARG);
        String endDateStr = parsedArgs.get(END_DATE_ARG);
        
        try {
            DateRange dateRange = DateRange.fromStrings(initDateStr, endDateStr);
            log.info(messageService.getMessage("cli.args.daterange.created", dateRange));
            return dateRange;
        } catch (IllegalArgumentException e) {
            log.error("Error al crear el rango de fechas: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Verifica si se solicitó ayuda
     */
    public boolean isHelpRequested() {
        return helpRequested;
    }
    
    /**
     * Muestra la ayuda de uso
     */
    public void printHelp() {
        String help = """
            
            ╔══════════════════════════════════════════════════════════════════════════════╗
            ║                           Comdirect Document Sync                           ║
            ╚══════════════════════════════════════════════════════════════════════════════╝
            
            DESCRIPCIÓN:
              Descarga documentos (PDFs) de la API de Comdirect en un rango de fechas.
            
            USO:
              java -jar comdirect-sync.jar [OPCIONES]
              ./run-dev.sh [OPCIONES]
              ./run-prod.sh [OPCIONES]
            
            OPCIONES:
              --init-date=YYYY-MM-DD    Fecha inicial para la descarga
                                        Si no se especifica, usa el primer día del mes actual
              
              --end-date=YYYY-MM-DD     Fecha final para la descarga  
                                        Si no se especifica, usa la fecha actual
              
              --help                    Muestra esta ayuda
            
            EJEMPLOS:
              # Descargar documentos desde el 1 de junio hasta el 20 de septiembre de 2025
              ./run-prod.sh --init-date=2025-06-01 --end-date=2025-09-20
              
              # Descargar documentos desde el 1 de junio hasta hoy
              ./run-prod.sh --init-date=2025-06-01
              
              # Descargar documentos del mes actual (desde el día 1 hasta hoy)
              ./run-prod.sh
            
            TIPOS DE DOCUMENTOS:
              Por defecto se buscan: Dividendengutschrift, Buchungsanzeige, 
              Finanzreport, Steuermitteilung, Ertragsgutschrift
              
              Configure DOCUMENT_PATTERN en el archivo .env para personalizar.
            
            DIRECTORIO DE DESCARGA:
              Los documentos se guardan en: downloads/documents/
              Configure DOWNLOAD_DIRECTORY en .env para personalizar.
            
            """;
        
        System.out.println(help);
    }
    
    /**
     * Verifica si hay argumentos relacionados con fechas
     */
    public boolean hasDateArguments() {
        return parsedArgs.containsKey(INIT_DATE_ARG) || parsedArgs.containsKey(END_DATE_ARG);
    }
    
    /**
     * Obtiene un resumen de los argumentos parseados
     */
    public String getArgsSummary() {
        if (parsedArgs.isEmpty()) {
            return "Sin argumentos de fecha especificados (usando valores por defecto)";
        }
        
        StringBuilder summary = new StringBuilder("Argumentos parseados: ");
        parsedArgs.forEach((key, value) -> 
            summary.append(key).append("=").append(value).append(" ")
        );
        
        return summary.toString().trim();
    }
}