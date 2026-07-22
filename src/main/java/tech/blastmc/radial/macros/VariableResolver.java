package tech.blastmc.radial.macros;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class VariableResolver {

    public static String resolve(String command) {
        StringBuilder result = new StringBuilder(command.length());

        int index = 0;

        while (index < command.length()) {
            if (command.charAt(index) != '%') {
                result.append(command.charAt(index));
                index++;
                continue;
            }

            int end = findPlaceholderEnd(command, index);

            if (end == -1) {
                result.append(command.charAt(index));
                index++;
                continue;
            }

            String original = command.substring(index, end + 1);
            String expression = command.substring(index + 1, end);

            try {
                Object value = evaluate(expression);

                if (value == null)
                    result.append(original);
                else
                    result.append(value);
            } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException ex) {
                result.append(original);
            }

            index = end + 1;
        }

        return result.toString();
    }

    private static Object evaluate(String expression) throws ReflectiveOperationException {
        List<String> parts = splitTopLevel(expression.trim(), '.');

        if (parts.isEmpty() || parts.get(0).isBlank())
            throw new IllegalArgumentException("Empty variable expression");

        Base base = Base.byName(parts.get(0));

        if (base == null)
            throw new IllegalArgumentException("Unknown base variable: " + parts.get(0));

        Object current = base.get();

        for (int i = 1; i < parts.size(); i++) {
            if (current == null)
                return null;

            current = resolveMember(current, parts.get(i));
        }

        return current;
    }

    private static Object resolveMember(Object target, String expression) throws ReflectiveOperationException {
        expression = expression.trim();

        if (expression.isEmpty())
            throw new IllegalArgumentException("Empty member expression");

        int openingParenthesis = expression.indexOf('(');

        if (openingParenthesis == -1)
            return readField(target, expression);

        if (!expression.endsWith(")"))
            throw new IllegalArgumentException("Invalid method expression: " + expression);

        String methodName = expression.substring(0, openingParenthesis).trim();

        if (methodName.isEmpty())
            throw new IllegalArgumentException("Empty method name");

        String argumentsText = expression.substring(openingParenthesis + 1, expression.length() - 1);

        List<Object> arguments = new ArrayList<>();

        if (!argumentsText.isBlank())
            for (String argument : splitTopLevel(argumentsText, ','))
                arguments.add(parseArgument(argument.trim()));

        return invokeMethod(target, methodName, arguments);
    }

    private static Object readField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), fieldName);

        if (field == null)
            throw new NoSuchFieldException(target.getClass().getName() + "." + fieldName);

        if (Modifier.isStatic(field.getModifiers()))
            throw new IllegalArgumentException("Static fields are not supported: " + fieldName);

        if (!field.canAccess(target) && !field.trySetAccessible())
            throw new IllegalAccessException("Could not access field: " + field);

        return field.get(target);
    }

    private static Field findField(Class<?> type, String fieldName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass())
            try {return current.getDeclaredField(fieldName);}
            catch (NoSuchFieldException ignored) { }

        return null;
    }

    private static Object invokeMethod(Object target, String methodName, List<Object> arguments) throws ReflectiveOperationException {
        List<MethodCandidate> candidates = new ArrayList<>();

        for (Method method : collectMethods(target.getClass())) {
            if (!method.getName().equals(methodName))
                continue;

            if (method.isBridge() || method.isSynthetic())
                continue;

            if (Modifier.isStatic(method.getModifiers()))
                continue;

            if (method.getReturnType() == void.class)
                continue;

            if (method.isVarArgs())
                continue;

            if (method.getParameterCount() != arguments.size())
                continue;

            ArgumentConversion conversion = convertArguments(method.getParameterTypes(), arguments);

            if (conversion == null)
                continue;

            candidates.add(new MethodCandidate(method, conversion.arguments(), conversion.score()));
        }

        if (candidates.isEmpty())
            throw new NoSuchMethodException("%s.%s(%d)".formatted(target.getClass().getName(), methodName, arguments.size()));

        candidates.sort(Comparator.comparingInt(MethodCandidate::score));

        int bestScore = candidates.getFirst().score();

        List<MethodCandidate> bestCandidates = candidates.stream()
                .filter(candidate -> candidate.score() == bestScore)
                .toList();

        MethodCandidate selected = selectMostSpecific(bestCandidates);

        if (selected == null)
            throw new IllegalArgumentException("Ambiguous method call: %s.%s".formatted(target.getClass().getName(), methodName));

        Method method = selected.method();

        if (!method.canAccess(target) && !method.trySetAccessible())
            throw new IllegalAccessException("Could not access method: " + method);

        try {
            return method.invoke(target, selected.arguments());
        } catch (InvocationTargetException ex) {
            throw ex;
        }
    }

    private static List<Method> collectMethods(Class<?> type) {
        Map<MethodSignature, Method> methods = new LinkedHashMap<>();

        for (Class<?> current = type; current != null; current = current.getSuperclass())
            for (Method method : current.getDeclaredMethods())
                methods.putIfAbsent(MethodSignature.of(method), method);

        for (Method method : type.getMethods())
            methods.putIfAbsent(MethodSignature.of(method), method);

        return new ArrayList<>(methods.values());
    }

    private static ArgumentConversion convertArguments(Class<?>[] parameterTypes, List<Object> arguments) {
        Object[] converted = new Object[arguments.size()];
        int score = 0;

        for (int index = 0; index < parameterTypes.length; index++) {
            Class<?> originalParameterType = parameterTypes[index];
            Class<?> parameterType = wrap(originalParameterType);
            Object argument = arguments.get(index);

            if (argument == null) {
                if (originalParameterType.isPrimitive())
                    return null;

                converted[index] = null;
                score += 100;
                continue;
            }

            Class<?> argumentType = argument.getClass();

            if (parameterType.equals(argumentType)) {
                converted[index] = argument;
                continue;
            }

            if (parameterType == Double.class && argument instanceof Integer integer) {
                converted[index] = integer.doubleValue();
                score += 10;
                continue;
            }

            if (parameterType.isAssignableFrom(argumentType)) {
                converted[index] = argument;
                score += inheritanceDistance(argumentType, parameterType);
                continue;
            }

            return null;
        }

        return new ArgumentConversion(converted, score);
    }

    private static MethodCandidate selectMostSpecific(List<MethodCandidate> candidates) {
        if (candidates.size() == 1)
            return candidates.getFirst();

        MethodCandidate selected = null;

        for (MethodCandidate candidate : candidates) {
            boolean moreSpecificThanAll = true;

            for (MethodCandidate other : candidates) {
                if (candidate == other)
                    continue;

                if (!isMoreSpecific(candidate.method(), other.method())) {
                    moreSpecificThanAll = false;
                    break;
                }
            }

            if (!moreSpecificThanAll)
                continue;

            if (selected != null)
                return null;

            selected = candidate;
        }

        return selected;
    }

    private static boolean isMoreSpecific(Method first, Method second) {
        Class<?>[] firstParameters = first.getParameterTypes();
        Class<?>[] secondParameters = second.getParameterTypes();

        boolean foundStrictlyMoreSpecificParameter = false;

        for (int index = 0; index < firstParameters.length; index++) {
            Class<?> firstType = wrap(firstParameters[index]);
            Class<?> secondType = wrap(secondParameters[index]);

            if (firstType.equals(secondType))
                continue;

            if (!secondType.isAssignableFrom(firstType))
                return false;

            foundStrictlyMoreSpecificParameter = true;
        }

        return foundStrictlyMoreSpecificParameter;
    }

    private static int inheritanceDistance(Class<?> child, Class<?> parent) {
        if (child.equals(parent))
            return 0;

        Queue<TypeDistance> queue = new ArrayDeque<>();
        Set<Class<?>> visited = new HashSet<>();

        queue.add(new TypeDistance(child, 0));
        visited.add(child);

        while (!queue.isEmpty()) {
            TypeDistance current = queue.remove();

            Class<?> superclass = current.type().getSuperclass();

            if (superclass != null && visited.add(superclass)) {
                if (superclass.equals(parent))
                    return current.distance() + 1;

                queue.add(new TypeDistance(superclass, current.distance() + 1));
            }

            for (Class<?> interfaceType : current.type().getInterfaces()) {
                if (!visited.add(interfaceType))
                    continue;

                if (interfaceType.equals(parent))
                    return current.distance() + 1;

                queue.add(new TypeDistance(interfaceType, current.distance() + 1));
            }
        }

        return 1000;
    }

    private static Object parseArgument(String argument) throws ReflectiveOperationException {
        if (argument.isEmpty())
            throw new IllegalArgumentException("Empty method argument");

        if (argument.startsWith("%")) {
            int end = findPlaceholderEnd(argument, 0);

            if (end == argument.length() - 1) {
                return evaluate(
                        argument.substring(1, argument.length() - 1)
                );
            }
        }

        if (argument.equals("null"))
            return null;

        if (argument.equals("true"))
            return true;

        if (argument.equals("false"))
            return false;

        if (isQuotedString(argument))
            return parseString(argument);

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException ignored) { }

        try {
            return Double.parseDouble(argument);
        } catch (NumberFormatException ignored) { }

        throw new IllegalArgumentException("Unsupported method argument: " + argument);
    }

    private static boolean isQuotedString(String value) {
        return value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"';
    }

    private static String parseString(String value) {
        String contents = value.substring(1, value.length() - 1);
        StringBuilder result = new StringBuilder(contents.length());

        boolean escaped = false;

        for (int index = 0; index < contents.length(); index++) {
            char character = contents.charAt(index);

            if (!escaped) {
                if (character == '\\')
                    escaped = true;
                else
                    result.append(character);

                continue;
            }

            switch (character) {
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                default -> result.append(character);
            }

            escaped = false;
        }

        if (escaped)
            result.append('\\');

        return result.toString();
    }

    private static int findPlaceholderEnd(String input, int openingPercent) {
        int parentheses = 0;
        boolean escaped = false;
        boolean quoted = false;

        for (int index = openingPercent + 1; index < input.length(); index++) {
            char character = input.charAt(index);

            if (quoted) {
                if (escaped) {
                    escaped = false;
                    continue;
                }

                if (character == '\\') {
                    escaped = true;
                    continue;
                }

                if (character == '"')
                    quoted = false;

                continue;
            }

            if (character == '"') {
                quoted = true;
                continue;
            }

            if (character == '(') {
                parentheses++;
                continue;
            }

            if (character == ')') {
                parentheses--;

                if (parentheses < 0)
                    return -1;

                continue;
            }

            if (character != '%')
                continue;

            if (parentheses == 0)
                return index;


            int nestedEnd = findPlaceholderEnd(input, index);

            if (nestedEnd == -1)
                return -1;

            index = nestedEnd;
        }

        return -1;
    }

    private static List<String> splitTopLevel(String input, char delimiter) {
        List<String> result = new ArrayList<>();

        int start = 0;
        int parentheses = 0;
        boolean escaped = false;
        boolean quoted = false;

        for (int index = 0; index < input.length(); index++) {
            char character = input.charAt(index);

            if (quoted) {
                if (escaped) {
                    escaped = false;
                    continue;
                }

                if (character == '\\') {
                    escaped = true;
                    continue;
                }

                if (character == '"')
                    quoted = false;

                continue;
            }

            if (character == '"') {
                quoted = true;
                continue;
            }

            if (character == '%') {
                int nestedEnd = findPlaceholderEnd(input, index);

                if (nestedEnd == -1)
                    throw new IllegalArgumentException("Unclosed nested variable");

                index = nestedEnd;
                continue;
            }

            if (character == '(') {
                parentheses++;
                continue;
            }

            if (character == ')') {
                parentheses--;

                if (parentheses < 0)
                    throw new IllegalArgumentException("Unexpected closing parenthesis");

                continue;
            }

            if (character == delimiter && parentheses == 0) {
                result.add(input.substring(start, index).trim());

                start = index + 1;
            }
        }

        if (quoted)
            throw new IllegalArgumentException("Unclosed string");

        if (parentheses != 0)
            throw new IllegalArgumentException("Unclosed parenthesis");

        result.add(input.substring(start).trim());

        return result;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive())
            return type;

        if (type == boolean.class)
            return Boolean.class;

        if (type == byte.class)
            return Byte.class;

        if (type == short.class)
            return Short.class;

        if (type == int.class)
            return Integer.class;

        if (type == long.class)
            return Long.class;

        if (type == float.class)
            return Float.class;

        if (type == double.class)
            return Double.class;

        if (type == char.class)
            return Character.class;

        return type;
    }

    private record ArgumentConversion(Object[] arguments, int score) { }

    private record MethodCandidate(Method method, Object[] arguments, int score) { }

    private record MethodSignature(String name, List<Class<?>> parameterTypes) {

        private static MethodSignature of(Method method) {
            return new MethodSignature(method.getName(), Arrays.asList(method.getParameterTypes()));
        }
    }

    private record TypeDistance(Class<?> type, int distance) { }

    @AllArgsConstructor
    enum Base {
        MINECRAFT("minecraft") {
            @Override
            Object get() {
                return Minecraft.getInstance();
            }
        },
        TARGET_ENTITY("targetEntity") {
            @Override
            Object get() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.hitResult instanceof EntityHitResult entityHit)
                    return entityHit.getEntity();

                return null;
            }
        },
        NEAREST_ENTITY("nearestEntity") {
            @Override
            Object get() {
                Minecraft mc = Minecraft.getInstance();

                if (mc.player == null || mc.level == null)
                    return null;

                return mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(10), entity -> entity.isAlive() && !entity.isSpectator())
                        .stream()
                        .min(Comparator.comparingDouble(entity -> mc.player.distanceToSqr(entity)))
                        .orElse(null);
            }
        },
        ;

        @Getter
        final String name;

        abstract Object get();

        static Base byName(String name) {
            for (Base base : values())
                if (base.name.equals(name))
                    return base;

            return null;
        }
    }

}
