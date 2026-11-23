package randy.framework.repository;

import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.beans.PropertyDescriptor;
import java.util.*;

public abstract class RGenericRepository<T> {

    protected final JdbcTemplate jdbc;
    protected final Class<T> type;
    protected final String table;
    protected final RowMapper<T> mapper;

    protected RGenericRepository(JdbcTemplate jdbc, Class<T> type, String table) {
        this.jdbc = jdbc;
        this.type = type;
        this.table = table;
        this.mapper = new BeanPropertyRowMapper<>(type);
    }

    protected Set<String> generatedColumns() {
        return Set.of("id", "created_at");
    }

    public List<T> findAll() {
        return jdbc.query("SELECT * FROM " + table + " ORDER BY id", mapper);
    }

    public Optional<T> findById(int id) {
        return jdbc.query("SELECT * FROM " + table + " WHERE id = ?", mapper, id)
                .stream().findFirst();
    }

    public List<T> findBy(String column, Object value) {
        return jdbc.query("SELECT * FROM " + table + " WHERE " + column + " = ?", mapper, value);
    }

    public Optional<T> findOneBy(String column, Object value) {
        return findBy(column, value).stream().findFirst();
    }

    public T save(T entity) {
        Map<String, Object> values = columnValues(entity);
        String cols = String.join(", ", values.keySet());
        String marks = String.join(", ", Collections.nCopies(values.size(), "?"));
        Integer id = jdbc.queryForObject(
                "INSERT INTO " + table + " (" + cols + ") VALUES (" + marks + ") RETURNING id",
                Integer.class, values.values().toArray());
        writeProperty(entity, "id", id);
        return entity;
    }

    public int update(T entity) {
        Map<String, Object> values = columnValues(entity);
        String set = String.join(", ", values.keySet().stream().map(c -> c + " = ?").toList());
        List<Object> args = new ArrayList<>(values.values());
        args.add(readProperty(entity, "id"));
        return jdbc.update("UPDATE " + table + " SET " + set + " WHERE id = ?", args.toArray());
    }

    public int deleteById(int id) {
        return jdbc.update("DELETE FROM " + table + " WHERE id = ?", id);
    }

    private Map<String, Object> columnValues(T entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(type)) {
            if (pd.getReadMethod() == null || pd.getWriteMethod() == null) continue;
            String column = toColumn(pd.getName());
            if (generatedColumns().contains(column)) continue;
            values.put(column, readProperty(entity, pd.getName()));
        }
        return values;
    }

    private Object readProperty(T entity, String name) {
        try {
            return BeanUtils.getPropertyDescriptor(type, name).getReadMethod().invoke(entity);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeProperty(T entity, String name, Object value) {
        try {
            BeanUtils.getPropertyDescriptor(type, name).getWriteMethod().invoke(entity, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toColumn(String property) {
        return property.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}