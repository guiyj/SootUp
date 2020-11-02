package de.upb.swt.soot.core.validation;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Linghui Luo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.core.model.SootClass;
import java.util.List;

/** Implement this interface if you want to provide your own class validator */
public interface ClassValidator {
  /**
   * Validates the given class and saves all validation errors in the given list.
   *
   * @param sc the class to check
   */
  void validate(SootClass sc, List<ValidationException> exceptions);

  /**
   * Basic validators run essential checks and are run always if validate is called.<br>
   * If this methodRef returns false and the caller of the validator respects this property,<br>
   * the checks will only be run if the debug or validation option is activated.
   *
   * @return whether this validator is a basic validator
   */
  boolean isBasicValidator();
}
