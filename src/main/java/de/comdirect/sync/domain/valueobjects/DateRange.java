package de.comdirect.sync.domain.valueobjects;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * Value Object que representa un rango de fechas para filtrar documentos.
 * Implementa la lógica de valores por defecto:
 * - Si initDate es null, usa el primer día del mes actual
 * - Si endDate es null, usa la fecha actual
 */
public class DateRange {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final LocalDate initDate;
    private final LocalDate endDate;
    
    /**
     * Constructor principal que valida el rango de fechas
     */
    public DateRange(LocalDate initDate, LocalDate endDate) {
        this.initDate = Objects.requireNonNull(initDate, "La fecha inicial no puede ser null");
        this.endDate = Objects.requireNonNull(endDate, "La fecha final no puede ser null");
        
        if (initDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                String.format("La fecha inicial (%s) no puede ser posterior a la fecha final (%s)", 
                    initDate, endDate)
            );
        }
    }
    
    /**
     * Constructor que aplica valores por defecto según las reglas de negocio
     */
    public static DateRange createWithDefaults(LocalDate initDate, LocalDate endDate) {
        LocalDate effectiveInitDate = initDate != null ? initDate : getFirstDayOfCurrentMonth();
        LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now();
        
        return new DateRange(effectiveInitDate, effectiveEndDate);
    }
    
    /**
     * Constructor que parsea fechas desde String con formato yyyy-MM-dd
     */
    public static DateRange fromStrings(String initDateStr, String endDateStr) {
        LocalDate initDate = parseDate(initDateStr);
        LocalDate endDate = parseDate(endDateStr);
        
        return createWithDefaults(initDate, endDate);
    }
    
    /**
     * Constructor para el mes actual completo
     */
    public static DateRange currentMonth() {
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        return new DateRange(firstDay, now);
    }
    
    private static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                String.format("Formato de fecha inválido: '%s'. Use el formato yyyy-MM-dd", dateStr), e
            );
        }
    }
    
    private static LocalDate getFirstDayOfCurrentMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }
    
    // Getters
    public LocalDate getInitDate() {
        return initDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    /**
     * Convierte las fechas al formato de string esperado por la API de Comdirect
     */
    public String getInitDateAsString() {
        return initDate.format(DATE_FORMAT);
    }
    
    public String getEndDateAsString() {
        return endDate.format(DATE_FORMAT);
    }
    
    /**
     * Verifica si una fecha está dentro del rango
     */
    public boolean contains(LocalDate date) {
        return date != null && 
               !date.isBefore(initDate) && 
               !date.isAfter(endDate);
    }
    
    /**
     * Retorna la cantidad de días en el rango
     */
    public long getDayCount() {
        return java.time.temporal.ChronoUnit.DAYS.between(initDate, endDate) + 1;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRange dateRange = (DateRange) o;
        return Objects.equals(initDate, dateRange.initDate) && 
               Objects.equals(endDate, dateRange.endDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(initDate, endDate);
    }
    
    @Override
    public String toString() {
        return String.format("DateRange{initDate=%s, endDate=%s, days=%d}", 
            getInitDateAsString(), getEndDateAsString(), getDayCount());
    }
}