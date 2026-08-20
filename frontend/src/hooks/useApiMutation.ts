import { useMutation, useQueryClient, type UseMutationOptions, type QueryKey } from '@tanstack/react-query';

interface UseApiMutationOptions<TData, TVariables>
  extends Omit<UseMutationOptions<TData, Error, TVariables>, 'mutationFn'> {
  // liste koje treba osvežiti posle uspešne mutacije (npr. ['suppliers'] posle kreiranja dobavljača)
  invalidateKeys?: QueryKey[];
}

export function useApiMutation<TData, TVariables>(
  mutationFn: (variables: TVariables) => Promise<TData>,
  options?: UseApiMutationOptions<TData, TVariables>,
) {
  const queryClient = useQueryClient();
  const { invalidateKeys, ...rest } = options ?? {};

  return useMutation({
    mutationFn,
    ...rest,
    onSuccess: (...args: Parameters<NonNullable<UseMutationOptions<TData, Error, TVariables>['onSuccess']>>) => {
      invalidateKeys?.forEach((key) => queryClient.invalidateQueries({ queryKey: key }));
      rest.onSuccess?.(...args);
    },
  });
}
