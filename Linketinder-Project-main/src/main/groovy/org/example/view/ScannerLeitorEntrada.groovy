package org.example.view

/**
 *
 * @author Guilherme Lima Conte
 */

class ScannerLeitorEntrada implements LeitorEntrada{

    final Scanner scanner

    ScannerLeitorEntrada(Scanner scanner) {
        this.scanner = scanner
    }

    @Override
    String lerLinha(String mensagem) {
        print mensagem
        return  scanner.nextLine()
    }
}
