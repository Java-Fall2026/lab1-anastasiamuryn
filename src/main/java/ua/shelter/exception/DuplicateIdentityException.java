package ua.shelter.exception;

public class DuplicateIdentityException extends IllegalStateException  {
         public DuplicateIdentityException(String identity, String repositoryName) {
        super("Duplicate identity '" + identity + "' in repository '" + repositoryName + "'");
    }
}
