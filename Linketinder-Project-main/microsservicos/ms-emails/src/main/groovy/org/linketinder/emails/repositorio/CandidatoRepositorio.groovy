package org.linketinder.emails.repositorio

import java.sql.SQLException

/**
 *
 * @author Guilherme Lima Conte
 */

interface CandidatoRepositorio {

    List<String> listarEmails() throws SQLException
}
