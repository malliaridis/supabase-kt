# supabase-kt

This is an unofficial Supabase client for Kotlin Multiplatform Projects (KMP).

## Build and Execution

You should be able to build the project like any other project. If you have any issues, feel free to open a ticket.

## Publish Library

### Prepare library for publication

When you are about to publish the library you can run the following command to check if everything builds as expected:

```bash
gradle check
```

### Publish to `mavenLocal`

If the library is alright, and you want to publish the library to your local maven repository, you can simply run:

```bash
gradle publishToMavenLocal
```

When publishing the library to `mavenLocal` you should find the generated files in your user directory under
`~/.m2/repository/io.supabase`. There, every artifact / module that was published is versioned.

### Make version available to others

To avoid version conflicts and any issues related to invalid or wrong versions of the library, we should always share
the specific version that is used in any other project. So, whenever you create a new version and want to use that
version in a project, create a tag and push it to the repository (project versioning has to be discussed prior to that).
