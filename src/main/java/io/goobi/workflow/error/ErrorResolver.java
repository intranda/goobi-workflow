/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package io.goobi.workflow.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import lombok.extern.log4j.Log4j2;

/** Resolves a throwable or an HTTP status to an error catalogue entry. */
@Log4j2
public class ErrorResolver {

    private final List<ErrorMapper> mappers;

    public ErrorResolver(List<ErrorMapper> mappers) {
        this.mappers = List.copyOf(mappers);
    }

    private static final class Holder {
        private static final ErrorResolver INSTANCE = load();

        private Holder() {
        }
    }

    private static ErrorResolver load() {
        try {
            return new ErrorResolver(
                    ServiceLoader.load(ErrorMapper.class, ErrorMapper.class.getClassLoader()).stream().map(ServiceLoader.Provider::get).toList());
        } catch (RuntimeException | Error e) { //NOSONAR the error path must never fail because of a broken mapper
            log.error("Could not load error mappers, using the built-in ones", e);
            return new ErrorResolver(List.of(new ViewExpiredMapper(), new DatabaseUnavailableMapper()));
        }
    }

    public static ErrorResolver fromServiceLoader() {
        return Holder.INSTANCE;
    }

    /**
     * @param throwable the failure, may be null
     * @param httpStatus the HTTP status, may be null
     * @return the resolved error, never null
     */
    public ResolvedError resolve(Throwable throwable, Integer httpStatus) {
        List<Throwable> chain = chainOf(throwable);
        for (Throwable t : chain) {
            if (t instanceof GoobiUserFacingException ufe) {
                return new ResolvedError(ufe.getEntry(), ufe.getParams(), ufe.getLogContext(), throwable);
            }
        }
        for (Throwable t : chain) {
            for (ErrorMapper mapper : mappers) {
                Optional<ErrorEntry> entry = mapSafely(mapper, t);
                if (entry.isPresent()) {
                    return new ResolvedError(entry.get(), List.of(), Map.of(), throwable);
                }
            }
        }
        ErrorEntry entry = ErrorCatalogue.UNEXPECTED;
        if (throwable == null && httpStatus != null) {
            if (httpStatus == 404) {
                entry = ErrorCatalogue.NOT_FOUND;
            } else if (httpStatus == 403) {
                entry = ErrorCatalogue.FORBIDDEN;
            }
        }
        return new ResolvedError(entry, List.of(), Map.of(), throwable);
    }

    private static Optional<ErrorEntry> mapSafely(ErrorMapper mapper, Throwable t) {
        try {
            Optional<ErrorEntry> result = mapper.map(t);
            return result == null ? Optional.empty() : result;
        } catch (RuntimeException e) { //NOSONAR a broken mapper must not break the error path
            log.warn("Error mapper {} failed and is skipped: {}", mapper.getClass().getName(), e.toString());
            return Optional.empty();
        }
    }

    static List<Throwable> chainOf(Throwable throwable) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Throwable> chain = new ArrayList<>();
        for (Throwable t = throwable; t != null && seen.add(t); t = t.getCause()) {
            chain.add(t);
        }
        return chain;
    }
}
