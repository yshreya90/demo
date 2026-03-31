# Harness CI/CD setup for this repo

This repository now contains the minimum assets needed for the take-home:

- `Dockerfile` for the native Harness **Build and Push** step.
- `k8s/values.yaml` plus `k8s/templates/*.yaml` for a Kubernetes **Canary** deployment in Harness CD.

## 1. Preconditions

Create these connectors in Harness Project Settings:

- Git connector for this repository.
- Artifactory Docker connector that points at your JFrog Docker registry.
- Kubernetes connector for your target cluster/namespace.

Create these core entities:

- Service: `harness-demo-svc`
- Environment: `dev`
- Infrastructure Definition for the target Kubernetes namespace

## 2. CI pipeline

Create pipeline `ci-build-pipeline`.

### Stage

- Stage type: `Build`
- Infrastructure: `Harness Cloud`

### Codebase

- Connect this repo.
- Use your default branch.

### Execution steps

1. `Run` step named `maven_test_and_package`

Use a Maven image that includes JDK 17, for example:

```yaml
image: maven:3.9.9-eclipse-temurin-17
```

Command:

```bash
mvn -B clean test package
```

2. `Build and Push an image to Docker Registry` step named `build_and_push`

- Registry/connector type: Artifactory Docker registry
- Connector: your Artifactory connector
- Repo: `<docker-repo>/<image-name>`
  Example: `demo-docker-local/harness-demo`
- Tags:
  - `<+pipeline.sequenceId>`
  - `latest`
- Dockerfile: `./Dockerfile`
- Context: `.`

This works because the Maven step creates `target/demo-0.0.1-SNAPSHOT.jar`, and the Dockerfile copies that jar into the runtime image.

## 3. CD pipeline

Create pipeline `cd-deploy-pipeline`.

### Stage

- Stage type: `Deployment`
- Deployment type: `Kubernetes`
- Service: `harness-demo-svc`
- Environment: `dev`

### Service definition

#### Manifests

Add a Kubernetes manifest source from this repo:

- Values file: `k8s/values.yaml`
- Templates folder: `k8s/templates`

#### Artifact

Add a primary artifact from Artifactory Docker registry:

- Source type: `Artifactory Registry`
- Connector: the same Artifactory connector or a CD artifact connector
- Image path: `<docker-repo>/<image-name>`
  Example: `demo-docker-local/harness-demo`
- Tag: runtime input or `<+trigger.artifact.build>`

`k8s/values.yaml` already references the artifact via:

```yaml
image: <+artifacts.primary.image>
```

### Infrastructure

- Kubernetes connector: your target cluster connector
- Namespace: the namespace where you want the app deployed
- Release name: `harness-demo`

### Execution

Choose strategy: `Canary`

Configure the default Harness-generated flow like this:

#### Canary phase

1. `Rollout Deployment`
   - Instance count: `1`
2. Optional `Verify`
3. Optional `Harness Approval`

#### Primary phase

1. `Rollout Deployment`
2. `Finalize Deployment`

Important: keep only one managed `Deployment` workload in the service manifests. That is already true with `k8s/templates/deployment.yaml`.

## 4. CI to CD trigger

If you want automatic promotion:

- Create a trigger on `cd-deploy-pipeline`
- Event source: successful completion of `ci-build-pipeline`
- Pass the built tag to the CD artifact tag

Use the CI image tag `<+pipeline.sequenceId>` in CI, and pass that same value into the CD artifact tag.

## 5. What to capture for submission

- Successful `ci-build-pipeline` execution summary
- Build and Push step logs
- Screenshot of the pushed image in Artifactory
- Successful `cd-deploy-pipeline` execution
- Evidence that Canary phase ran before Primary rollout
- The final unified execution URL if you configure CI to trigger CD

## 6. Notes specific to this repo

- The app exposes `GET /heartbeat`, which is used by the Kubernetes liveness and readiness probes.
- `pom.xml` was corrected from Spring Boot `4.0.5` to `4.0.3` because `4.0.5` is not present in Maven Central as of 2026-03-31.
